/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.suggestion;

import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.BaseSuggestionHandler;
import com.adr.bigdata.search.handler.query.frontend.SuggestionQuery;
import com.adr.qvh.getdata.GetTrainingSetForCategory;
import com.adr.qvh.logisticregression.LogisticRegressionClassifier;
import com.adr.qvh.logisticregression.ModelLR;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings("rawtypes")
public class LogisticRegressionSuggestion extends BaseSuggestionHandler {

	private Map<Integer, String> categories;

	private LogisticRegressionClassifier classifier;
	private GetTrainingSetForCategory gtsfc;

	private TrainStatistics history;

	private boolean isTraining = false;
	private boolean isGetData = false;

	private String dir;
	private boolean use2Gram;
	private boolean use3Gram;
	private boolean isAccent;

	@Override
	public void init(NamedList args) {
		NamedList configs = (NamedList) args.get("config");
		String model = getModelDir(configs);
		dir = getDir(configs);
		Thread syncronizer = new Thread(new Runnable() {

			@Override
			public void run() {
				initSyncronizer(dir);
			}
		});
		syncronizer.start();

		use2Gram = getUse2Gram(configs);
		use3Gram = getUse3Gram(configs);
		isAccent = getAccent(configs);
		categories = ConfigUtils.getCategory(configs);
		if (categories == null || categories.isEmpty()) {
			return;
		}

		try {
			log.info("initalizing logistic regression classifier");
			classifier = new LogisticRegressionClassifier(use2Gram, use3Gram, isAccent, dir);
			classifier.setModelLR((ModelLR) classifier.loadModel(new FileReader(model))); // look ugly
		} catch (Exception e) {
			removeIndexFile();
			getLogger().error("failed to load logistic regression classifier", e);
		}

		gtsfc = new GetTrainingSetForCategory();

		history = new TrainStatistics(null, 0, SuggestionQuery.Params.GET_DATA);
		history.setOtherInfo(null, 0, SuggestionQuery.Params.TRAIN);

		super.init(args);
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		String train = req.getParams().get(SuggestionQuery.Params.TRAIN);
		String getData = req.getParams().get(SuggestionQuery.Params.GET_DATA);
		if ("true".equalsIgnoreCase(train) && !isIsTrainingOrGetData()) {
			new Thread(new Trainer()).start();
			rsp.add("Status", "LOGISTIC REGRESSION CLASSIFIER IS TRAINING");
			setTrainingMessage(rsp);
			return;
		}
		if ("true".equalsIgnoreCase(getData) && !isIsTrainingOrGetData()) {
			new Thread(new GetData()).start();
			rsp.add("Status", "GETTING DATA");
			setGetDataMessage(rsp);
			return;
		}

		if ("status".equalsIgnoreCase(train)) {
			if (isIsTrainingOrGetData()) {
				rsp.add("Status", "TRAINING");
				setTrainingMessage(rsp);
			} else {
				rsp.add("Status", "FINISH TRAINING");
				setTrainingMessage(rsp);
			}

			return;
		} else if ("status".equalsIgnoreCase(getData)) {
			if (isIsTrainingOrGetData()) {
				rsp.add("Status", "GETTING DATA");
				setGetDataMessage(rsp);
			} else {
				rsp.add("Status", "FINISH GETTING DATA");
				setGetDataMessage(rsp);
			}

			return;
		}
		super.handleRequest(req, rsp);
	}

//	@Override
//	protected List<ClassifyResult> classify(String keyword) {
//		if (Strings.isNullOrEmpty(keyword) || classifier == null) {
//			getLogger().info("keyword is empty or classifier has not initiated yet");
//			return null;
//		}
//		try {
//			getLogger().debug("loggistic classify keyword: " + keyword);
//			long start = System.currentTimeMillis();
//			PredictObject[] classified = classifier.classify(keyword);
//			getLogger()
//					.debug("loggistic regression classification has taken {} ms", System.currentTimeMillis() - start);
//			if (classified == null) {
//				return null;
//			}
//			List<ClassifyResult> category = new LinkedList<>();
//			for (PredictObject o : classified) {
//				String categoryName = categories.get(o.getLabelId());
//				if (!Strings.isNullOrEmpty(categoryName))
//					category.add(new ClassifyResult(o.getLabelId(), categoryName, o.getProb()));
//			}
//			return category;
//		} catch (Exception ex) {
//			getLogger().error("failed to classify", ex);
//			return null;
//		}
//	}

	/**
	 * @author:minhvv2 the function start a file watcher to watch all the change
	 *                 of Logistic-regression file and syncronize to other host
	 *                 in the cluster
	 */
	private void initSyncronizer(String path) {
		ModelSyncronizor syncronizer = new ModelSyncronizor(path, System.getProperty("scp.user", "root"));
		syncronizer.watch();
	}

	private String getModelDir(NamedList configs) {
		return (String) configs.get("model");
	}

	public synchronized boolean isIsTrainingOrGetData() {
		return isTraining || isGetData;
	}

	public synchronized void setIsTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}

	public synchronized void setIsGetData(boolean isGetData) {
		this.isGetData = isGetData;
	}

	private void setTrainingMessage(SolrQueryResponse rsp) {
		rsp.add("Last train", history.toString());
	}

	private void setGetDataMessage(SolrQueryResponse rsp) {
		rsp.add("Last getData", history.toString());
	}

	private String getDir(NamedList configs) {
		return (String) configs.get("dir");
	}

	private boolean getUse2Gram(NamedList configs) {
		return ((String) configs.get("use2gram")).equals("true");
	}

	private boolean getUse3Gram(NamedList configs) {
		return ((String) configs.get("use3gram")).equals("true");
	}

	private boolean getAccent(NamedList configs) {
		return ((String) configs.get("accent")).equals("true");
	}

	private class Trainer implements Runnable {
		@Override
		public void run() {
			setIsTraining(true);
			Date trainDate = new Date();
			long start = System.currentTimeMillis();
			try {
				removeIndexFile();
				classifier = new LogisticRegressionClassifier(use2Gram, use3Gram, isAccent, dir);
				classifier.train("train_priname_catlevel_1", LogisticRegressionClassifier.TYPE_FILE_DOC);
				history.setStatusTrain(true);
			} catch (Exception ex) {
				getLogger().error("train error", ex);
				history.setStatusTrain(false);
			} finally {
				history.setTakenTimeTrain(System.currentTimeMillis() - start);
				history.setLastTrainTime(trainDate);
			}
			setIsTraining(false);
		}

	}

	private class GetData implements Runnable {

		@Override
		public void run() {
			setIsGetData(true);
			Date trainDate = new Date();
			long start = System.currentTimeMillis();
			try {
				String dbUrl = System.getProperty("database.mssql.url",
						"jdbc:sqlserver://10.220.75.95:1433;databaseName=Adayroi_CategoryManagement");
				String dbUser = System.getProperty("database.mssql.user", "adruserfortest");
				String dbPass = System.getProperty("database.mssql.password", "adruserfortest@qaz");

				getLogger().info("\n" + dir + "\n" + dbUrl + "\n" + dbUser + "\n" + dbPass);
				gtsfc.getAllData(dir, dbUrl, dbUser, dbPass);

				removeIndexFile();
				classifier = new LogisticRegressionClassifier(use2Gram, use3Gram, isAccent, dir);
				getLogger().info("\n" + dir);
				classifier.train("train_priname_catlevel_1", LogisticRegressionClassifier.TYPE_FILE_DOC);

				history.setStatusGetData(true);
			} catch (Exception ex) {
				getLogger().error("train error", ex);
				history.setStatusGetData(false);
			} finally {
				history.setTakenTimeGetData(System.currentTimeMillis() - start);
				history.setLastGetDataTime(trainDate);
			}
			setIsGetData(false);
		}

	}

	private void removeIndexFile() {
		File f = new File(dir + "/indexer.dat");
		if (f.exists())
			f.delete();
	}

	private static class TrainStatistics {

		private final static DateFormat DEVIL_FRUIT = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		private Date lastTrainTime;
		private long takenTimeTrain;
		private Boolean statusTrain = null;

		private Date lastGetDataTime;
		private long takenTimeGetData;
		private Boolean statusGetData = null;

		public TrainStatistics(Date lastTrainTime, long takenTime, String type) {
			switch (type) {
			case SuggestionQuery.Params.GET_DATA:
				this.lastTrainTime = lastTrainTime;
				this.takenTimeTrain = takenTime;
				break;
			case SuggestionQuery.Params.TRAIN:
				this.lastGetDataTime = lastTrainTime;
				this.takenTimeGetData = takenTime;
				break;
			}
		}

		public void setOtherInfo(Date lastTrainTime, long takenTime, String type) {
			switch (type) {
			case SuggestionQuery.Params.GET_DATA:
				this.lastTrainTime = lastTrainTime;
				this.takenTimeTrain = takenTime;
				break;
			case SuggestionQuery.Params.TRAIN:
				this.lastGetDataTime = lastTrainTime;
				this.takenTimeGetData = takenTime;
				break;
			}
		}

		public void setLastTrainTime(Date lastTrainTime) {
			this.lastTrainTime = lastTrainTime;
		}

		public void setTakenTimeTrain(long takenTime) {
			this.takenTimeTrain = takenTime;
		}

		public void setStatusTrain(Boolean status) {
			this.statusTrain = status;
		}

		public void setLastGetDataTime(Date lastTrainTime) {
			this.lastGetDataTime = lastTrainTime;
		}

		public void setTakenTimeGetData(long takenTime) {
			this.takenTimeGetData = takenTime;
		}

		public void setStatusGetData(Boolean status) {
			this.statusGetData = status;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Last train time: ").append(lastTrainTime != null ? DEVIL_FRUIT.format(lastTrainTime) : null)
					.append("\n").append("Taken time for train: ").append(takenTimeTrain).append("\n")
					.append("Success: ").append(statusTrain).append("\n");
			sb.append("Last get and train time: ")
					.append(lastGetDataTime != null ? DEVIL_FRUIT.format(lastGetDataTime) : null).append("\n")
					.append("Taken time for get and train: ").append(takenTimeGetData).append("\n").append("Success: ")
					.append("statusGetData").append("\n");
			return sb.toString();
		}
	}
}
