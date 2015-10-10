/**
 * 
 */
package com.adr.bigdata.updateprocessor.attributesearch;

/**
 * @author ndn
 *
 */
public class CategoryAttributeConfig {
	private int catId;
	private int attributeId;
	private String attributeName;
	private float weight;

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attributeId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategoryAttributeConfig other = (CategoryAttributeConfig) obj;
		if (attributeId != other.attributeId)
			return false;
		return true;
	}

	public CategoryAttributeConfig() {
		super();
	}

	public CategoryAttributeConfig(int catId, int attributeId, String attributeName, float weight) {
		super();
		this.catId = catId;
		this.attributeId = attributeId;
		this.attributeName = attributeName;
		this.weight = weight;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public int getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "CategoryAttributeConfig [catId=" + catId + ", attributeId=" + attributeId + ", attributeName="
				+ attributeName + ", weight=" + weight + "]";
	}

	
	
}
