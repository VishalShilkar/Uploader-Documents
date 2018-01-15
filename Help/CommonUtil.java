package com.globeop.go2.appComponent.util;

import static com.globeop.go2.appComponent.GoMatchConstants.Database.GOCONFIRM;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globeop.go2.appComponent.GoMatchConstants.ARViewByFilter;
import com.globeop.go2.appComponent.GoMatchConstants.DashBoardItemStatus;
import com.globeop.go2.appComponent.RequestMappingConstants;
import com.globeop.go2.appComponent.dto.BaseDTO;
import com.globeop.go2.appComponent.dto.QueryParamDTO;
import com.globeop.go2.appComponent.dto.SearchRequestFormBean;
import com.globeop.go2.appComponent.dto.ValidationErrorDTO;
import com.globeop.go2.appComponent.exception.GomatchBaseException;
import com.globeop.go2.appComponent.interceptor.InterceptorHelper;
import com.globeop.go2.client.dto.FundDTO;
import com.globeop.go2.gmt.board.dto.BoardDTO;
import com.globeop.go2.gmt.paperConfirmsAwaitingAction.formBean.SignPageRequestFormBean;
import com.globeop.risk.web.util.StringUtil;
import com.lowagie.text.pdf.PdfReader;

/**
 * 
 * @author vshilkar
 * @since May 26, 2016
 * /home/cvsd/cvs/root/ematch-web/matching/src/com/globeop/go2/appComponent/service/BoardSearchQuery.java,v 1.1 2016/12/14 11:51:19 shsoni Exp $
 * CommonUtil.java
 */
public class CommonUtil {

	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);

	public static String drServerName = null;
	public static String quoppaPDFToImageConvertorKeyMainServer = null;
	public static String quoppaPDFToImageConvertorKeyDRServer = null;
	/*public static HashMap<String, Boolean> dealMap = new HashMap<String, Boolean>();

	static {
		dealMap.put("CAF",Boolean.TRUE);
		dealMap.put("CRE",Boolean.TRUE);
		dealMap.put("EQS",Boolean.TRUE);
		dealMap.put("FRA",Boolean.TRUE);
		dealMap.put("FXO",Boolean.TRUE);
		dealMap.put("EQO",Boolean.TRUE);
		dealMap.put("IRS",Boolean.TRUE);
		dealMap.put("SWO",Boolean.TRUE);
		dealMap.put("OPB",Boolean.TRUE);
		dealMap.put("BON",Boolean.FALSE);
		dealMap.put("EQU",Boolean.FALSE);
		dealMap.put("CFW",Boolean.FALSE);
		dealMap.put("FUT",Boolean.FALSE);
		dealMap.put("MBS",Boolean.FALSE);
		dealMap.put("OPT",Boolean.FALSE);
		dealMap.put("PCD",Boolean.FALSE);
		dealMap.put("WAR",Boolean.FALSE);
		dealMap.put("CAS",Boolean.FALSE);
		dealMap.put("CMP",Boolean.FALSE);
		dealMap.put("FWD",Boolean.FALSE);
		dealMap.put("SPT",Boolean.FALSE);
		dealMap.put("FXS",Boolean.FALSE);
		dealMap.put("IAM",Boolean.FALSE);
		dealMap.put("LAD",Boolean.FALSE);
		dealMap.put("REP",Boolean.FALSE);
		dealMap.put("SEC",Boolean.FALSE);
		dealMap.put("TRF",Boolean.FALSE);
	}*/
	/**
	 * @param value1
	 * @param value2
	 * @return true if value1 is equal to value2
	 */
	public static boolean isEqual(String value1, String value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if ((value1 == null && value2 != null) || (value2 == null && value1 != null)) {
			return false;
		}
		return value1.trim().equals(value2.trim()); 
	}

	/**
	 * @param value1
	 * @param value2
	 * @return true if value1 is equal to value2
	 */
	public static boolean isEqual(int value1, int value2) {
		return value1 == value2; 
	}

	/**
	 * @param value1
	 * @param value2
	 * @return true if value1 is equal to value2
	 */
	public static boolean isEqual(short value1, short value2) {
		return value1 == value2; 
	}

	/**
	 * @param value1
	 * @param value2
	 * @return true if value1 is equal to value2
	 */
	public static boolean isEqual(Object value1, Object value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if ((value1 == null && value2 != null) || (value2 == null && value1 != null)) {
			return false;
		}
		return value1.equals(value2); 
	}

	/**
	 * Checks if the given value is null
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Object obj) {
		return obj == null;
	}

	/**
	 * This method returns true if value is null or empty
	 * @param value
	 * @return boolean
	 */
	public static boolean isNullOrEmpty(String value) {
		return value == null ? true : value.trim().isEmpty(); 
	}

	/**
	 * This method returns true if collection is null or empty
	 * @param value
	 * @return boolean
	 */
	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null ? true : collection.isEmpty(); 
	}

	/**
	 * hverma..8:37:51 PM..@param confUserFundRole
	 * hverma..8:37:51 PM..@return
	 */
	public static boolean isNullOrEmpty(Map map) {
		return map == null ? true : map.isEmpty(); 
	}

	/**
	 * Trims the value if it is not null.
	 * @param value
	 * @return {@link String}
	 */
	public static String trim(String value) {
		return value == null ? null : value.trim();
	}

	public static String toString(Object obj){
		if (isNull(obj)) return "";

		StringBuilder sb = null;
		try {
			List<Field> fields = Arrays.asList(obj.getClass().getDeclaredFields());
			if (isNullOrEmpty(fields)) return "";

			sb = new StringBuilder();
			sb.append("[");
			sb.append(obj.getClass().getName());
			sb.append(" :: ");

			for (Field f : fields){
				if ("serialVersionUID".equals(f.getName())){
					continue;
				}
				f.setAccessible(true);
				if (!isNull(f.get(obj))) {
					sb.append(f.getName() + "="+f.get(obj) + ", ");
				}
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), "]");
		} catch (SecurityException e) {
			log.info(""+e.getStackTrace());
		} catch (IllegalArgumentException e) {
			log.info(""+e.getStackTrace());
		} catch (IllegalAccessException e) {
			log.info(""+e.getStackTrace());
		}
		return isNull(sb) ? "" : sb.toString();
	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static boolean validateBaseDTOForSuccessStatus(BaseDTO baseDTO) {
		if (baseDTO != null && baseDTO.getServiceStatus() != null && baseDTO.getServiceStatus().equals(BaseDTO.SUCCESS))
		{
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static boolean validateBaseDTOForSuccessWithNoDataStatus(BaseDTO baseDTO) {
		if (baseDTO != null && baseDTO.getServiceStatus() != null && baseDTO.getServiceStatus().equals(BaseDTO.SUCCESS_NO_DATA)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static boolean validateBaseDTOForSystemFailurStatus(BaseDTO baseDTO) {
		if (baseDTO != null && baseDTO.getServiceStatus() != null && baseDTO.getServiceStatus().equals(BaseDTO.SYSTEM_FAILURE)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static boolean validateBaseDTOForBussinessValidationStatus(BaseDTO baseDTO) {
		if (baseDTO != null && baseDTO.getServiceStatus() != null && baseDTO.getServiceStatus().equals(BaseDTO.BUSINESS_VALIDATION_FAILURE)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static boolean validateBaseDTOForWarning(BaseDTO baseDTO) {
		if (baseDTO != null && baseDTO.getServiceStatus() != null && baseDTO.getServiceStatus().equals(BaseDTO.WARNING))
		{
			return true;
		}
		return false;
	}
	
	public static BaseDTO populateBaseDTOErrorDetailList(GomatchBaseException e, BaseDTO baseDTO){

		if(CommonUtil.isNull(baseDTO)){
			baseDTO = new BaseDTO();
		}
		baseDTO.setGomatchErrorDetail(e.getErrorDetail());
		baseDTO.setServiceStatus(BaseDTO.SYSTEM_FAILURE);
		return baseDTO;

	}
	
	public static BaseDTO populateBaseDTOErrorDetailList(Exception e, BaseDTO baseDTO){

		if(CommonUtil.isNull(baseDTO)){
			baseDTO = new BaseDTO();
		}
		baseDTO.setServiceStatus(BaseDTO.SYSTEM_FAILURE);
		return baseDTO;

	}

	/**
	 *
	 * @param baseDTO
	 * @return
	 */
	public static List<String> populateErrorMessages(BaseDTO baseDTO){

		List<ValidationErrorDTO> validationErrorList = baseDTO.getValidationErrorList();
		List<String> messages = new ArrayList<String>();

		for (int i = 0; i < validationErrorList.size(); i++) {
			ValidationErrorDTO validationErrorDTO  = (ValidationErrorDTO) validationErrorList.get(i);
			messages.add(validationErrorDTO.getErrorMessage());
		}

		return messages;
	}

	public static SearchRequestFormBean getSearchRequestFormBeanFromJSON(String jsonRequestData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			//JSON from String to Object
			SearchRequestFormBean obj = mapper.readValue(jsonRequestData, SearchRequestFormBean.class);
			return obj;
		} catch (IOException e) {
			CommonUtil.loggerErrorMessage(log, "getSearchRequestFormBeanFromJSON", e);
		}
		return null;
	}

	public static SignPageRequestFormBean getSignPageRequestFormBeanFromJSON(String jsonRequestData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			//JSON from String to Object
			SignPageRequestFormBean obj = mapper.readValue(jsonRequestData, SignPageRequestFormBean.class);
			return obj;
		} catch (IOException e) {
			CommonUtil.loggerErrorMessage(log, "getSignPageRequestFormBeanFromJSON", e);
		}
		return null;
	}

	public static String createHyperLink(String value, String param ,String funtioneName){
		String hrefUrl="";
		if(isNullOrEmpty(value)){
			return hrefUrl;
		}
		if("0".equalsIgnoreCase(value)){
			return value;
		}
		
		
		
		if(value!=null && !value.equalsIgnoreCase("")){
			try {
				hrefUrl="<a style='text-decoration: inherit !important;' href="+"javascript:void(0)"+" onclick="+funtioneName+"("+param+")>"+value+"</a>";
			} catch (Exception e) {
				CommonUtil.loggerErrorMessage(log, "createHyperLink", e);
			}
		}
		return hrefUrl;
	}
	
	public static String createLink(String value, String param ,String funtionName){
		String hrefUrl="";
		if(isNullOrEmpty(value)){
			return hrefUrl;
		}
		if(value!=null && !value.equalsIgnoreCase("")){
			try {
				hrefUrl="<a class='plainText' href=\"javascript:void(0)\" onclick=\""+funtionName+"("+param+")\">"+value+"</a>";
			} catch (Exception e) {
				CommonUtil.loggerErrorMessage(log, "createHyperLink", e);
			}
		}
		return hrefUrl;
	}
	
	public static String createLink(String value, String param ,String funtionName,String tooltipTitle){
		String hrefUrl="";
		if(isNullOrEmpty(value)){
			return hrefUrl;
		}
		if(value!=null && !value.equalsIgnoreCase("")){
			try {
				hrefUrl="<a style='text-decoration: inherit !important;' data-toggle='tooltip' title="+tooltipTitle+" class='plainText' href=\"javascript:void(0)\" onclick=\""+funtionName+"("+param+")\">"+value+"</a>";
			} catch (Exception e) {
				CommonUtil.loggerErrorMessage(log, "createHyperLink", e);
			}
		}
		return hrefUrl;
	}
	
	
	public static String createImgTag(String imgSrcURL){
		StringBuffer imgTag =  new StringBuffer();
		try {
			imgTag.append("<");
			imgTag.append("img");
			imgTag.append(" style='height: 50px;' src='");
			if(!isNullOrEmpty(imgSrcURL)){
				imgTag.append(imgSrcURL);
			}
			imgTag.append("' ");
			imgTag.append(">");
			imgTag.append("</img>");
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "createImgTag", e);
			return "<img src=''></img>";
		}
		return imgTag.toString();
	}
	
	public static String createPopUp(BoardDTO boardDTO, String value, String funtioneName, String className, String... param){
		return createPopUp(boardDTO.isInternal(), value, funtioneName, className, param);
	}

	public static String createPopUp(boolean isInternal, String value, String funtionName, String className, String... param){
		String hrefUrl="";
		String classes = "";
		if(isNullOrEmpty(value)){
			return hrefUrl;
		}
		if("0".equalsIgnoreCase(value)){
			return value;
		}
		if(!isNullOrEmpty(className)){
			classes = "class=\"" + className + "\"";
		}
		
		if(value!=null && !value.equalsIgnoreCase("")){
			try {
				if(!isInternal){
					hrefUrl="<a href="+"javascript:void(0)"+" " + classes + " data-toggle=\"popover\" data-trigger=\"hover\" data-content=\"<table><tr><th class='thHeader dynamicTable ellipsis spacing'>Client Value</th><th class='thHeader dynamicTable ellipsis spacing'>Counterparty Value</th></tr><tr><td class='dynamicTable ellipsis dataFormating'>"+param[0]+"</td><td class='dynamicTable ellipsis dataFormating'>"+param[1]+"</td></tr></table>\" onmouseover=\""+ funtionName+"('"+param[0]+"')" +"\" onclick="+funtionName+"('"+param[0]+"')>"+value+"</a>";
				}else{
					hrefUrl="<a style=\"text-decoration: none !important; color: red !important; background-color: #eaec38 !important;\" href=\"javascript:dialog_message('MisMatched Details' , 'Client Value: "+param[0]+", Counterparty Value: "+ param[1] +" ')\" onmouseleave=\"hide_dialog_message();\" onmouseover=\" dialog_message('MisMatched Details' , 'Client Value: "+param[0]+", Counterparty Value: "+ param[1] +" ') \" >"+value+"</a>";
				}
			} catch (Exception e) {
				CommonUtil.loggerErrorMessage(log, "createPopUp", e);
			}
		}
		return hrefUrl;
	}

	public static Date convertEstDate(Date estDate, String timeZone){
		Date newDate = null;
		Calendar calendar1 = null;
		Calendar calendar2 = null;

		if(estDate!=null){
			calendar1 = new GregorianCalendar();
			calendar1.setTime(estDate);
			int date = calendar1.get(Calendar.DATE);
			int month = calendar1.get(Calendar.MONTH);
			int year = calendar1.get(Calendar.YEAR);
			int hour = calendar1.get(Calendar.HOUR_OF_DAY);
			int minute = calendar1.get(Calendar.MINUTE);

			if(timeZone.equalsIgnoreCase("US(EST)")){calendar2 = calendar1;}
			else if(timeZone.equalsIgnoreCase("UK(BST)")){
				SimpleTimeZone britTime = new SimpleTimeZone(0, "Europe/London", Calendar.MARCH, -1, Calendar.SUNDAY, 1*60*60*1000, Calendar.OCTOBER, -1, Calendar.SUNDAY, 2*60*60*1000);
				calendar2 = new GregorianCalendar(britTime, Locale.UK);
				calendar2.set(Calendar.DATE, date);
				calendar2.set(Calendar.MONTH, month);
				calendar2.set(Calendar.YEAR, year);
				calendar2.set(Calendar.HOUR_OF_DAY, hour);
				calendar2.set(Calendar.MINUTE, minute);
			}else if(timeZone.equalsIgnoreCase("IN(IST)")){
				calendar2 = new GregorianCalendar(TimeZone.getTimeZone("IST"));
				calendar2.set(Calendar.DATE, date);
				calendar2.set(Calendar.MONTH, month);
				calendar2.set(Calendar.YEAR, year);
				calendar2.set(Calendar.HOUR_OF_DAY, hour);
				calendar2.set(Calendar.MINUTE, minute);
			}

			newDate = calendar2.getTime();
		}

		return newDate;
	}

	public static String getStringValue(Object obj){
		if (obj == null) {
			return "";
		}
		if (obj instanceof String) {
			return ((String) obj).trim();
		} else {
			return String.valueOf(obj);
		}
	}
	
	public static BigDecimal getBigDecimalValue(Object obj){
		if (obj == null) {
			return BigDecimal.ZERO;
		}
		if (obj instanceof String) {
			return new BigDecimal(((String) obj).trim());
		} else {
			return new BigDecimal(String.valueOf(obj));
		}
	}
	
	public static Integer getIntegerValue(Object obj){
		if (obj == null) {
			return 0;
		}
		if (obj instanceof String) {
			return Integer.valueOf(((String) obj).trim());
		} else {
			return Integer.valueOf(String.valueOf(obj));
		}
	}

	public static String getCurrencyValue(BigDecimal bigDecimal){
		String convertValue="";
		if(isNull(bigDecimal)){
			return convertValue; 
		}
		try {
		bigDecimal=bigDecimal.setScale(3, RoundingMode.CEILING);
		convertValue=getStringValue(bigDecimal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertValue; 
	}
	
	/**
	 * vshilkar
	 * Oct 24, 2016
	 * convertCommaToCharacters
	 * String
	 * @param clientId
	 * @return
	 */
	public static String convertCommaToCharacters(String clientId) {
		return clientId.replace("%2C", ",");
	}

	/**
	 * vshilkar
	 * Nov 08, 2016
	 */
	public static List getPageviseResults(Integer pageNo, Integer rowCount, String scrollDir, List results){    	
		Integer startIndex = (pageNo-1)*rowCount;
		Integer endIndex = 0;
		Double totalPages = results.size()/rowCount.doubleValue();
		if (pageNo >= 0 && pageNo < Math.ceil(totalPages)) {
			endIndex = pageNo * rowCount;    
		}else{
			endIndex = results.size();
		}
		int count = Math.min(rowCount, results.size() - endIndex);   
		List returnList = results.subList(startIndex, endIndex);
		return returnList;
	}

	/**
	 * hverma 11-10-2016
	 * **/
	public static String enCodedString(String str){
		String response=null;
		if(str!=null || !str.equals("")){
			response=StringUtil.encodeHTML(str);
		}
		return response;
	}

	/**
	 * @param dbIdArray
	 * @return
	 */
	public static List<String>  convertToList(String dbIdArray,String delim) {
		List<String> list=new ArrayList<>();
		try {
			if(!isNullOrEmpty(dbIdArray) && !isNullOrEmpty(delim)){
				StringTokenizer stname=new StringTokenizer(dbIdArray,delim);
				while (stname.hasMoreTokens()) {
					String nam=stname.nextToken().toString();
					list.add(nam.trim());
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "convertToList", e);
		}
		return list;
	}

	/**
	 * @param refinedFundIds
	 * @param string
	 * @return
	 */
	public static String setListAsString(List<String> list, String delim) {
		String listAsString="";
		
		if(isNullOrEmpty(list)){
			return listAsString;	
		}

		int count=1;
		for (String string : list) {
			if(count>1){
				listAsString=listAsString.concat(delim+string);
			}else{
				listAsString=listAsString.concat(string); 
			}
			count++;
		}
		return listAsString;
	}

	/**
	 * @param refinedFundIds
	 * @param string
	 * @return
	 */
	public static String getFundIdsString(List<FundDTO> fundDTOs, String delim) {
		String listAsString="";
		if(isNullOrEmpty(fundDTOs)){
			return listAsString;
		}
		int count=1;
		for (int i=0; i<fundDTOs.size(); i++) {
			Integer fundId=fundDTOs.get(i).getId();
			if(count>1){
				listAsString=listAsString.concat(delim+String.valueOf(fundId));
			}else{
				listAsString=listAsString.concat(String.valueOf(fundId)); 
			}
			count++;
		}
		return listAsString;
	}
	/**
	 * @param refinedFundIds
	 * @param string
	 * @return
	 */
	public static String getSetAsString(Set<String> refinedFundIds, String delim) {
		String listAsString="";
		int count=1;
		for (String string : refinedFundIds) {
			if(count>1){
				listAsString=listAsString.concat(delim+string);
			}else{
				listAsString=listAsString.concat(string); 
			}
			count++;
		}
		return listAsString;
	}

	
	/**
	 * @param clientMap
	 * @param clientShortName
	 * @return
	 */
	public static String getKeyFromMap(Map<String, String> map,String value){
		String key=null;
		try {
			if(map!=null && map.size()>0 && value!=null && !value.trim().equalsIgnoreCase("")){
				for (Entry<String, String> entry : map.entrySet()) {
					if (entry.getValue().equalsIgnoreCase(value)) {
						key= entry.getKey();
						break;
					}
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getKeyFromMap", e);
		}
		return key;
	}

	/**
	 * @param arrayList
	 * @param fundMap
	 * @return
	 */
	public static boolean isParamValid(ArrayList<String> paramList,HashMap<String, String> paramMap){

		ArrayList<String> idsFromMap = new ArrayList<String>();
		Iterator<String> itr =paramMap.keySet().iterator();

		while(itr.hasNext())
		{
			idsFromMap.add( itr.next());
		}
		return idsFromMap.containsAll(paramList);
	}


	public static String getDynamicQuery(String paramList, ArrayList<QueryParamDTO> list, int sqlType) {
		String subQuery = "";
		String[] params = paramList.split(",");
		for (String param : params) {
			subQuery = subQuery + "?,";
			list.add(new QueryParamDTO(param, sqlType));
		}
		subQuery = subQuery.substring(0, subQuery.length()-1);
		if (params.length > 1) {
			subQuery = "in (" + subQuery + ")";
		} else {
			subQuery = "= " + subQuery;
		}
		return subQuery;
	}
	
	public static String getStaticQuery(String param, ArrayList<QueryParamDTO> list, int sqlType) {
		String subQuery = "?";
		list.add(new QueryParamDTO(param, sqlType));
		return subQuery;
	}
	
	
	public static boolean setToggleFlag(String str){
		if(GOCONFIRM.equalsIgnoreCase(str)){
			return false;
		}else{
			return true;
		}
	}

	public static String convertDatetoString(String str) {
		String returnVal = null;
		try {
			SimpleDateFormat formatr = new SimpleDateFormat("yyyyMMdd");
			Date d = formatr.parse(str);
			formatr = new SimpleDateFormat("yyyy-MM-dd");
			returnVal = formatr.format(d);
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "convertDatetoString", e);
		}
		return returnVal;
	}
	
	public static String convertDatetoString(String str, String format) {
		String returnVal = null;
		try {
			SimpleDateFormat formatr = new SimpleDateFormat(format);
			Date d = formatr.parse(str);
			formatr = new SimpleDateFormat(format);
			returnVal = formatr.format(d);
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "convertDatetoString", e);
		}
		return returnVal;
	}
	
	public static String convertDatetoString(Date date, String format) {
		String returnVal = null;
		try {
			SimpleDateFormat formatr = new SimpleDateFormat(format);
			formatr = new SimpleDateFormat(format);
			returnVal = formatr.format(date);
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "convertDatetoString", e);
		}
		return returnVal;
	}
	
	public static String getTimeDifference(String methodName, Timestamp begin, Timestamp end) {
		long diff = end.getTime() - begin.getTime();
		long s = diff/1000;
		long ms = diff % 1000;
		long min = s / 60;
		s = s%60;
		return (methodName + " - Begin = "+ begin + ", End = " + end + ", Duration (min:sec:ms) = " + min+":" + s +":"+ms);
	}
	
	public static String truncateFilename(String filename){
        String newFilename = "";
        if(filename!=null){
            filename = filename.trim();
            newFilename = filename.length()<=50 ? filename : filename.substring(0, 25) + "..." + filename.substring(filename.length()-25);
        }
        return newFilename;
    }
	
	public static String get_formatted_list_short(List<Short> list) throws Exception {
		String str = "", item_str = "";
		for (Short item : list) {
			item_str = item.toString();
			str += "," + item_str;
		}
		if (str.startsWith(",")) {
			str = str.substring(1);
		}
		return str;
	}

	public static String get_formatted_list_integer(List<Integer> list) throws Exception {
		String str = "", item_str = "";
		for (Integer item : list) {
			item_str = item.toString();
			str += "," + item_str;
		}
		if (str.startsWith(",")) {
			str = str.substring(1);
		}
		return str;
	}

	public static String get_formatted_list_string(List<String> list) throws Exception {
		String str = "", item_str = "";
		for (String item : list) {
			item_str = item.toString();
			str += "," + item_str;
		}
		if (str.startsWith(",")) {
			str = str.substring(1);
		}
		return str;
	}
	
	public static String removeHTMLTags(String val){
		return val.replaceAll("\\<.*?>","").replace("&nbsp;", "");
	}
	
	public static String getGTId(String gTId){
		String [] tmp = gTId.split(":");
		return tmp[0].trim().toUpperCase()+": "+tmp[1].trim().toUpperCase();
	}
	
	public static String trimP_F(String capSysTId){
		int len = 0 ;
		len = capSysTId.length();
		capSysTId = capSysTId.substring(0,len-1);
		return capSysTId.trim();
	}
	
	/**
	 *
	 * @param logger
	 * @param methodName
	 * @param mode
	 */
	public static void loggerMessage(Logger logger,String methodName,String mode){

		logger.info("Method Name :: "+methodName+" :: "+mode);
	}

	/**
	 *
	 * @param logger
	 * @param methodName
	 * @param throwable
	 */
	public static void loggerErrorMessage(Logger logger,String methodName,Throwable throwable){

		logger.error("Exception in Method Name :: "+methodName, throwable);
	}
	
	public static String getFileExt(String fileName ){
		String fileExtension = null;
		if(!isNullOrEmpty(fileName)){
			fileExtension=(fileName.substring(fileName.indexOf(".")+1)).toUpperCase();
		}
		return 	fileExtension;
	}
	
	/**
	 * This method decodes the UTF-8 encoded URL.
	 * @param url
	 * @return decoded url
	 */
	public static String decodeURL(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (Exception e) {
			log.info("Unable to decode url: " + url);
		}
		return url;
	}
	
	
	/**
	 * Add the integers saved as String.
	 * @param value1 - first {@link Integer} stored as {@link String}
	 * @param value2 - second {@link Integer} stored as {@link String}
	 * @return addition of numbers with result as {@link String}
	 */
	public static String add(String value1, String value2) {
		return String.valueOf(Integer.parseInt(value1) + Integer.parseInt(value2));
	}
	
	/**
	 * Get the business date before nth business days
	 * @param businessDays
	 * @return {@link Date} representation of business date
	 */
	public static Date getBusinessDate(int businessDays) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		int dayOfWeek = 0;
		int days = businessDays;

		while (days > 0) {
			calendar.add(Calendar.DATE, -1);
			dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
				days--;
			}
		}
		log.info(businessDays + " business days: " + calendar.getTime());
		return calendar.getTime();
	}
    
    public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(i + "A: " + getBusinessDate(i));
			System.out.println(i +"B: "+calculateBusinessDay(i));
		}
	}
    
    public static boolean isSSNCSIte(HttpServletRequest request){
		return InterceptorHelper.isSSNCSite(request);
    }
    
    public static List<String> splitEqually(String text, int size){
        List<String> ret = new ArrayList<String>((text.length()+size-1)/size);
        for(int start=0; start<text.length(); start+=size)
        {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

	public static String getColumnNameByViewType(String viewBy) {		
		switch (viewBy) {
		case ARViewByFilter.STATUS:
			return "Confirm$Status";
		case ARViewByFilter.FUND:
			return "Fund";
		case ARViewByFilter.TRADE_TYPE:
			return "Trade$Type";
		case ARViewByFilter.COUNTERPARTY:
			return "Counterparty";
		case ARViewByFilter.EXECUTING_BROKER:
			return "Executing$Broker";
		case ARViewByFilter.CLEARING_BROKER:
			return "Clearing$Broker";
		default:
			return "Confirm$Status";
		}
	}
	
	public static String convertListToString(List list , String delima){
		StringBuffer returnStr = null;
		try {
			if(!CommonUtil.isNull(list) && list.size()>0){
				returnStr = new StringBuffer();
				for (Object object : list) {
					returnStr.append(String.valueOf(object)).append(delima);
				}
				returnStr = returnStr.deleteCharAt(returnStr.length()-1);
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "CommonUtil :: convertListToString", e);
		}
		return returnStr.toString();
	}
	
	public static int getPdfPages(String filePath) {
		PdfReader reader = null;
		try {
			reader = new PdfReader(filePath);
			return	reader.getNumberOfPages();
		} catch (IOException e) {
			CommonUtil.loggerErrorMessage(log, "CommonUtil :: getPdfPages", e);
		} 
		return 0;
	}

	public static String addHTMLCenterTag(String value) {
		return "<center>" + trim(value) + "</center>";
	}
	
	public static String addHTMLXMPTag(String value) {
		return "<xmp>" + trim(value) + "</xmp>";
	}
	
	public static String getDashBoardUpdateMessage(String updatedStatus){
		if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENT_REJECTED)){
			return RequestMappingConstants.ValidationMessage.tradeRejectSuccessfully;
		}else if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENT_REVIEWED)){
			return RequestMappingConstants.ValidationMessage.tradeReviwedSuccessfully;
		}else if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENTSIGN)){
			return RequestMappingConstants.ValidationMessage.tradeReInstantiatedToCSSuccessfully;
		}else if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENTPENDING)){
			return RequestMappingConstants.ValidationMessage.tradeReInstantiatedToCPSuccessfully;
		}else{
			return RequestMappingConstants.ValidationMessage.defaultMessage;
		}
	}
	
	public static String getDashBoardUpdateErrorMessage(String updatedStatus){
			if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENT_REJECTED)){
				return RequestMappingConstants.ValidationMessage.tradeRejectFail;
			}else if(updatedStatus.equalsIgnoreCase(DashBoardItemStatus.CLIENT_REVIEWED)){
				return RequestMappingConstants.ValidationMessage.tradeReviewFail;
			}else if(updatedStatus.contains(DashBoardItemStatus.CLIENTSIGN) || updatedStatus.contains(DashBoardItemStatus.CLIENTPENDING)){
				return RequestMappingConstants.ValidationMessage.tradeReInstantiatedFail;
			}else if(updatedStatus.contains(DashBoardItemStatus.PREEXECUTEDCP) && updatedStatus.contains(DashBoardItemStatus.PREEXECUTEDCS)){
				return RequestMappingConstants.ValidationMessage.tradeAlreadyExecuted;
			}else if(updatedStatus.contains(DashBoardItemStatus.CLIENT_REVIEWED) && updatedStatus.contains(DashBoardItemStatus.CLIENT_REJECTED)){
				return RequestMappingConstants.ValidationMessage.tradeAlreadyReviewed;
			}else{
				return RequestMappingConstants.ValidationMessage.defaultErrorMessage;
			}
	}
	
	public static String removeHiddenCharacters(String val){
		return val.replaceAll("[\u0000-\u001f]", "");
	}
	
	public static String removeFunctions(String val){
		return val.replace(",function (elem, startFrom) {        var startFrom = startFrom || 0;        if (startFrom > this.length) return -1;         for (var i = 0; i < this.length; i++) {            if (this[i] == elem && startFrom <= i) {                return i;            } else if (this[i] == elem && startFrom > i) {                return -1;            }        }        return -1;    }", "");
	}
	
	public static String getTradeType(String trade){
		String tradeType="";
		 StringTokenizer st = new StringTokenizer(trade,":");  
	     while (st.hasMoreTokens()) {
	    	 tradeType = st.nextToken();
	         break;
	     }  
		return tradeType;
	}

	/*public static boolean isOtc(String tradeType) {
		return dealMap.get(tradeType.trim());
	}
	
	public static String getConfirmType(String tradeEventTypeId, String tradeEventId, boolean isOtc, boolean isPartial){
		String tradeEventType = null, dashBoardItemType = null;

		if (tradeEventTypeId.equalsIgnoreCase("T") && isPartial){
			tradeEventType = "P";
		}else if (tradeEventTypeId.equalsIgnoreCase("A") && isPartial){
			tradeEventType = "N";
		}else{
			tradeEventType = tradeEventTypeId;
		}

		if (isOtc){
			if (tradeEventType.trim().equalsIgnoreCase("D")){
				dashBoardItemType = "25";
			}else if (tradeEventType.trim().equalsIgnoreCase("T")){
				dashBoardItemType = "26";
			}else if (tradeEventType.trim().equalsIgnoreCase("A")){
				dashBoardItemType = "27";
			}else if (tradeEventType.trim().equalsIgnoreCase("N")){
				dashBoardItemType = "28";
			}else if (tradeEventType.trim().equalsIgnoreCase("P")){
				dashBoardItemType = "29";
			}else if (tradeEventType.trim().equalsIgnoreCase("E")){
				dashBoardItemType = "31";
			}
		}else{
			if (tradeEventType.trim().equalsIgnoreCase("D")){
				dashBoardItemType = "25";
			}else if (tradeEventType.trim().equalsIgnoreCase("T")){
				dashBoardItemType =  "26";
			}
		}
		return dashBoardItemType;
	}*/
	
	public static String getDate(String formatType){
    	DateFormat formatter = new SimpleDateFormat(formatType);
    	return formatter.format(new Date(System.currentTimeMillis()));
    }
	
	public static String calculateBusinessDay(int ageBucket){
		Calendar cal = Calendar.getInstance();
		java.util.Date endDate = cal.getTime();
		cal.add(Calendar.DATE, -ageBucket);
		java.util.Date startDate = cal.getTime();
		int workingday = getWorkingDaysBetweenTwoDates(startDate,endDate);
		
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -(ageBucket+workingday));
		
		SimpleDateFormat s = new SimpleDateFormat("MM/dd/YYYY");
		String fromDate = s.format(cal.getTime());
		return fromDate;
	}
	
	public static int getWorkingDaysBetweenTwoDates(Date startDate, Date endDate) 
	{
	    Calendar startCal = Calendar.getInstance();
	    startCal.setTime(startDate);        

	    Calendar endCal = Calendar.getInstance();
	    endCal.setTime(endDate);

	    int workDays = 0;

	    if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
	        return 0;
	    }

	    if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
	        startCal.setTime(endDate);
	        endCal.setTime(startDate);
	    }

	    do {
	        startCal.add(Calendar.DAY_OF_MONTH, 1);
	        if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
	            ++workDays;
	        }
	    } while (startCal.getTimeInMillis() < endCal.getTimeInMillis()); 

	    return (workDays);
	}
	
}
