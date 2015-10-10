# Introduction

- *light-search* là solr plugin dùng giao tiếp API search cho frontend
- *light-search* sẽ nhận tham số API do frontend truyền vào và thực hiện parse sang các query solr cần thiết để lấy kết quả trả ra.
- Ngoài ra *light-search* sẽ lấy dữ liệu từ hazelcast để hiển thị cho mục filter trên trang frontend


# Installation
- Tiền điều kiện
	- Một HazelCast server đã được cài đặt và fetch đầy đủ dữ liệu.
	  Thực hiện thay đổi cấu hình hazelcast trong file: dist/big-data/light-search/hazelcast-client.xml (bao gồm IP, usr/pass)
	- Một mario-consumer được cài đặt để thực hiện realtime indexing dữ liệu
- Cài đặt
	- Bước 1: giải nén file setup solr "solr-{version}.tgz" vào thư mục "/opt" (hiện đang dùng solr-5.1.0)
	- Bước 2: pull code trên master branch của project light-search từ địa chỉ http://10.220.48.97/anhth/light-search.git
	- Bước 3: copy toàn bộ 2 thư mục product và deal trong project light-search vào thư mục /opt/solr-{version}/server/solr/product và /opt/solr-{version}/server/solr/deal
	- Bước 4: Copy thư viện ProductTransformer-<version>.jar và sqljdbc4-3.0.jar vào thư mục /contrib/datatimporthandler. Hai thư viện này đều được đẩy lên ivy server (máy chủ 133) 
	- Bước 5: build code và copy toàn bộ thư mục dist/light-search vào thư mục opt/solr-{version}/dist/big-data/light-search/
	- Bước 6: cấu hình hazelcast trong file cấu hình "conf/hazelcast-client.xml"
		- name and password của hazelcast group, tại *group* element
		- ip address, port của mancenter, tại *management-center* element
		- port của hazelcast instance, tại *network -> port* element
		- ip address của hazelcast instance, tại *network -> join -> tcp-ip* element
    - Bước 7: sửa cấu hình kết nối đến db tại thư mục conf/db.properties
    - Bước 8: Run /bin/solr start -m 8g
		(trong đó 8g là dung lượng heapsize của ứng dụng solr)

# Troubleshoot