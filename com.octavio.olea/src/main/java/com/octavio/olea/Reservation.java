package com.octavio.olea;

import java.time.LocalDateTime;

public class Reservation {
    private String clientName;
    private int tableSize;
    private LocalDateTime dateTime;
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public int getTableSize() {
		return tableSize;
	}
	public void setTableSize(int tableSize) {
		this.tableSize = tableSize;
	}
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}

