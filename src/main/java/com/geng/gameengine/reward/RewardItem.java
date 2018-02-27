/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年10月8日 上午10:57:55
 */
package com.geng.gameengine.reward;

/**
 * 奖励物品类
 */
public class RewardItem {
	private String item;
	private String rate;
	private int number;
	
	/**
	 * @param item
	 * @param rate
	 * @param number
	 */
	public RewardItem(String item, String rate, int number) {
		super();
		this.item = item;
		this.rate = rate;
		this.number = number;
	}
	/**
	 * @return the item
	 */
	public String getItem() {
		return item;
	}
	/**
	 * @param item the item to set
	 */
	public void setItem(String item) {
		this.item = item;
	}
	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}
	/**
	 * @param rate the rate to set
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}
}
