package com.globeop.go2.appComponent.util;



import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DateUtil {
	
	
	
	private static List<DateFormat> dateFormat  = new ArrayList<DateFormat>();
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	
	
	static{
		dateFormat.add(new SimpleDateFormat("MM/dd/yyyy HH:mm"));
		dateFormat.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"));
		dateFormat.add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
	}
	
	
	/**
	 * 
	 * This function will return Today's Date,representing the default 
	 * Calendar's time value (millisecond offset from the Epoch").
	 * @author Govardhan
	 * @return java.util.Date
	 * @since v2.0
	 */
	
	public static Date getToday()
	{
		
		Calendar calen2 = Calendar.getInstance();
		return calen2.getTime();
		
	}
	
	

	/**
	 * 
	 * This function will return the difference in days between Today's Date and the given Date.
	 * @author Govardhan
	 * @param sdate java.util.Date  2005-10-10
	 * @return int
	 * @since v2.0 
	 */
	
	public static int printDiff(java.util.Date sdate)
    {
      
		Calendar calen2 = Calendar.getInstance();
		Calendar  calen1=Calendar.getInstance(); 
        Date date  = sdate;
        Date todaydate= calen2.getTime();
        
      
        calen1.setTime(date);          
        
        
        calen2.setTime(todaydate);
        
        int days;
    	int daysUntilToday;
        
        days  = (int)(date.getTime()/(24*3600000)); //60*60*1000*24
        daysUntilToday  = (int)(todaydate.getTime()/(24*3600000));

       
        
        return ( daysUntilToday - days);
        
        
    }

/**
 *
 * This Function should return String in specified Date format as passed in a parameter.
 * @author Akshay
 * @function name formatSQLDate()
 * @param utilDate Date - 2004-10-01
 * @param format String - yyyy-MM-dd
 * @return String - 2004-10-01
 * @since v2.0 
 */
	
public static String getDateFromSQlDate(java.sql.Date utilDate, String format) {
  String sDate = "";
  try {
    if (utilDate == null) {
      throw new Exception("Please Enter valid utilDate");
    }
    if (format == null || format.trim().length() == 0) {
      throw new Exception("Please Enter valid String format");
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    sDate = dateFormat.format(utilDate);
  }
  catch (Exception e) {
    //System.out.println(e.getMessage());
  }
  return sDate;
}


/**
*
* This Function should return String in specified Date format passed as a parameter.
* @author Akshay
* @function name formatSQLDate()
* @param utilDate Date - 2004-10-01
* @param format String - yyyy-MM-dd
* @return String - 2004-10-01
* @since v2.0
*/
	
public static String getDateFromUtilDate(java.util.Date utilDate, String format) {
 String sDate = "";
 try {
   if (utilDate == null) {
     throw new Exception("Please Enter valid utilDate");
   }
   if (format == null || format.trim().length() == 0) {
     throw new Exception("Please Enter valid String format");
   }
   SimpleDateFormat dateFormat = new SimpleDateFormat(format);
     sDate = dateFormat.format(utilDate);
 }
 catch (Exception e) {
   //System.out.println(e.getMessage());
 }
 return sDate;
}
    

/**
 * This function will return the java.sql.Timestamp date in the form of a String,
 *  in a specified format, passed as a parameter.
 *  
 * @author Govardhan
 * @param sqlTime java.sql.Timestamp
 * @param format String
 * @return String
 * @since v2.0
 */


    public static String getDateFromTimeStamp(java.sql.Timestamp sqlTime, String format){

    	String sDate="";    	
    	SimpleDateFormat ft = new SimpleDateFormat(format);		
		sDate = ft.format(sqlTime);    	    	
    	return sDate;
    	
    }
   
    
    /**
     * This function will return Today's Date in dd-MMM-yyyy format.
     * @author Govardhan
     * @return java.util.Date 02-Mar-2005
     * @since v2.0 
     */
    
    public static String getTodaysDate() {
        Calendar calen = Calendar.getInstance();
        java.util.Date tempDate = calen.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String sDate = dateFormat.format(tempDate);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        try{
        tempDate = df.parse(sDate);
        }
        catch(Exception e){
        	
        }
               
        return sDate;
      }

    public static String getPreviousDate(String noOfDays)
    {
    	Calendar date = Calendar.getInstance();
    	Format formatter = new SimpleDateFormat("dd-MMM-yyyy");
    	String previousDate = null;
    	
    	if(noOfDays	!=	null)
    	{
    		date.add(Calendar.DATE, -Integer.parseInt(noOfDays.trim()));
    		previousDate	=	formatter.format(date.getTime());
    		
    	}
    	
    	return previousDate;
    }
    
    public static String getPreviousWorkingDate(String noOfDays)
    {
		Calendar date = Calendar.getInstance();
		Format formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String previousDate = null;
		int counter = 0;
		// System.out.println(date.getTime() + " ** " +
		// date.get(date.DAY_OF_WEEK) + " ** " + counter);
		while (counter < Integer.parseInt(noOfDays.trim())) {
			date.add(Calendar.DATE, -1);
			if (date.get(date.DAY_OF_WEEK) != 1
					&& date.get(date.DAY_OF_WEEK) != 7) {
				counter++;
			}
		}
		previousDate = formatter.format(date.getTime());

		return previousDate;
	}
    
    public static String getformattedDate(String date){
		Date convertDate = null;
		String convertedDate = null;
		if(date!=null && !" ".equals(date)){
			for(DateFormat format : dateFormat)
			{
				try {
					format.setLenient(false);
					convertDate = format.parse(date);
					DateFormat dateFormatNeeded = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					convertedDate = dateFormatNeeded.format(convertDate);
					break;
				} catch (Exception e) {
					System.out.println("*************************** Not able to Parse date********  "+e);
					}			
			}
		}
		
		return convertedDate;
	}
	
	public static String getFormatDate(Date convertDate){
		
		String convertedDate = null;
		if(convertDate!= null){
				try{
					DateFormat dateFormatNeeded = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					dateFormatNeeded.setLenient(false);
					convertedDate = dateFormatNeeded.format(convertDate);
				}
				catch(Exception e){
				System.out.println("*************************** Not able to convert date ********  "+e);
				}	
		}
		
		return convertedDate;
	}
	
	/* Below method will validate date format */
	
	public static boolean isValidDate(String date){
		boolean flag 	= false;
		Date parsedDate = null;
		if(date == null || "".equalsIgnoreCase(date)){
			return flag;
		}else{
			
			for(DateFormat format : dateFormat){
				try{
					
					format.setLenient(false);
					parsedDate = format.parse(date);
					break;
					
				}catch(Exception ee){
					
					System.out.println("Following date is not in valid format ::: "+date);
				}
			}
		}
		
		if(parsedDate!=null){
			flag = true;
		}
		
		return flag;
	}
  
	public static Timestamp convertStringToTimestamp(String str_date) {
		try {
			DateFormat formatter;
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// you can change format of date
			Date date = formatter.parse(str_date);
			java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());

			return timeStampDate;
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static Date convertStringToDateTime(String dateString) throws ParseException{
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = formatter.parse(dateString);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String formatDateToSimple(String dateStr, String currentFormat, String requiredFormat){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(currentFormat);
			Date date = (Date)dateFormat.parse(dateStr);
			
			dateFormat = new SimpleDateFormat(requiredFormat);
			dateStr = dateFormat.format(date);
			return dateStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Date getConvertedDate(String dateInString){
		SimpleDateFormat dateInSDF = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			Date date = new Date(dateInSDF.parse(dateInString).getTime());
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}

