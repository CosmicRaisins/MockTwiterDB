package ds4300;

public class DateTime {
  private int year;
  private int month;
  private int day;

  public DateTime(int year, int month, int day) {

    if (year >= 0) {
      this.year = year;
    } else {
      throw new IllegalArgumentException("Year cannot be negative.");
    }

    if (month < 13 && month > 0) {
      this.month = month;
    } else {
      throw new IllegalArgumentException("Illeagal Month.");
    }

    if (day < 32 && day > 0) {
      this.day = day;
    } else {
      throw new IllegalArgumentException("Illeagal day.");
    }
  }

  public DateTime(String dateTime) {
    this(dateTime.split(" ")[0].split("-"));
  }

  public DateTime(String[] dateTime) {
    this(dateTime[0], dateTime[1], dateTime[2]);
  }

  public DateTime(String year, String month, String day) {
    this(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
  }

  /**
   * is this Datetime before or the same as that Datetime?
   * @param that the other Datetime
   * @return whether this Datetime is before or equal to the other Datetime.
   */
  public boolean isBefore(DateTime that) {
    if (this.year == that.year) {
      if (this.month == that.month) {
        return this.day <= that.day;
      }
      return this.month < that.month;
    }
    return this.year < that.year;
  }

  @Override
  public String toString() {
    return String.format("%s-%s-%s", this.year, this.month, this.day);
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }
}
