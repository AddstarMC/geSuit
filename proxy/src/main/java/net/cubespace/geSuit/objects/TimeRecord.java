package net.cubespace.geSuit.objects;

public class TimeRecord {
    private String uuid;

	private long timeTotal;
	private long timeSession;
    private long timeToday;
    private long timeWeek;
    private long timeMonth;
    private long timeYear;

    public TimeRecord(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getTimeTotal() {
		return timeTotal;
	}

	public void setTimeTotal(long timeTotal) {
		this.timeTotal = timeTotal;
	}

	public long getTimeSession() {
		return timeSession;
	}

	public void setTimeSession(long timeSession) {
		this.timeSession = timeSession;
	}

	public long getTimeToday() {
		return timeToday;
	}

	public void setTimeToday(long timeToday) {
		this.timeToday = timeToday;
	}

	public long getTimeWeek() {
		return timeWeek;
	}

	public void setTimeWeek(long timeWeek) {
		this.timeWeek = timeWeek;
	}

	public long getTimeMonth() {
		return timeMonth;
	}

	public void setTimeMonth(long timeMonth) {
		this.timeMonth = timeMonth;
	}

	public long getTimeYear() {
		return timeYear;
	}

	public void setTimeYear(long timeYear) {
		this.timeYear = timeYear;
	}
}