package com.globeop.go2.appComponent.service;

import static com.globeop.go2.client.util.OrderBy.Direction.ASCENDING;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.globeop.gmt.data.xml.GoConfirmTradeDTO.Response.Results.PagedQueryResults.ResultsA.GoConfirmDashBooardItemTradeDTO;
import com.globeop.go2.appComponent.GoMatchConstants;
import com.globeop.go2.appComponent.GoMatchConstants.AliasType;
import com.globeop.go2.appComponent.GoMatchConstants.ConfTradeCDSDTOIndex;
import com.globeop.go2.appComponent.GoMatchConstants.ConfirmMethod;
import com.globeop.go2.appComponent.GoMatchConstants.DashBoardItemType;
import com.globeop.go2.appComponent.GoMatchConstants.Database;
import com.globeop.go2.appComponent.GoMatchConstants.EmatchDataMap;
import com.globeop.go2.appComponent.GoMatchConstants.Framework;
import com.globeop.go2.appComponent.GoMatchConstants.MatchingWeb;
import com.globeop.go2.appComponent.GoMatchConstants.Platform;
import com.globeop.go2.appComponent.GoMatchConstants.SearchModeId;
import com.globeop.go2.appComponent.GoMatchConstants.TradeTypeId;
import com.globeop.go2.appComponent.GoMatchConstants.UserType;
import com.globeop.go2.appComponent.RequestMappingConstants;
import com.globeop.go2.appComponent.LdapService.EntitlementService;
import com.globeop.go2.appComponent.annotations.NonLoggable;
import com.globeop.go2.appComponent.dao.CommonSQLExecuterDAO;
import com.globeop.go2.appComponent.dao.ConfObjectQueryDAO;
import com.globeop.go2.appComponent.dao.ConfTrackerStatusDTO;
import com.globeop.go2.appComponent.dao.SavedSearchDAO;
import com.globeop.go2.appComponent.dto.ApplicationNameDTO;
import com.globeop.go2.appComponent.dto.BaseDTO;
import com.globeop.go2.appComponent.dto.ConfActivityDTO;
import com.globeop.go2.appComponent.dto.ConfExternalSystemStatusDTO;
import com.globeop.go2.appComponent.dto.ConfTradeCDSDTO;
import com.globeop.go2.appComponent.dto.DashBoardItemFormBean;
import com.globeop.go2.appComponent.dto.DefaultColumnsDTO;
import com.globeop.go2.appComponent.dto.Filter;
import com.globeop.go2.appComponent.dto.GeneralDTO;
import com.globeop.go2.appComponent.dto.JQgridDTO;
import com.globeop.go2.appComponent.dto.KeyValueDTO;
import com.globeop.go2.appComponent.dto.ListDTO;
import com.globeop.go2.appComponent.dto.MyEntitlementDTO;
import com.globeop.go2.appComponent.dto.ResponseDTO;
import com.globeop.go2.appComponent.dto.SavedSearchDTO;
import com.globeop.go2.appComponent.dto.TradeDetailsDTO;
import com.globeop.go2.appComponent.dto.UserComparator;
import com.globeop.go2.appComponent.dto.UserRights;
import com.globeop.go2.appComponent.dto.UserSessionDTO;
import com.globeop.go2.appComponent.dto.ValidationErrorDTO;
import com.globeop.go2.appComponent.exception.GomatchBaseException;
import com.globeop.go2.appComponent.exception.GomatchErrorDetail;
import com.globeop.go2.appComponent.exception.GomatchExceptionHandler;
import com.globeop.go2.appComponent.exceptions.AbstractService;
import com.globeop.go2.appComponent.fundlevelpermission.Usersetting.Clients.Client;
import com.globeop.go2.appComponent.interceptor.InterceptorHelper;
import com.globeop.go2.appComponent.service.menu.Menu;
import com.globeop.go2.appComponent.service.menu.MenuService;
import com.globeop.go2.appComponent.util.CommonUtil;
import com.globeop.go2.appComponent.util.DateUtil;
import com.globeop.go2.appComponent.util.SessionUtil;
import com.globeop.go2.client.ClientServiceManager;
import com.globeop.go2.client.GmtClientServiceManager;
import com.globeop.go2.client.dto.AliasTypeDTO;
import com.globeop.go2.client.dto.AttachmentDTO;
import com.globeop.go2.client.dto.CentralClearingDTO;
import com.globeop.go2.client.dto.ClientDTO;
import com.globeop.go2.client.dto.CounterpartyDTO;
import com.globeop.go2.client.dto.CustomTradeTypeDTO;
import com.globeop.go2.client.dto.DashBoardDTO;
import com.globeop.go2.client.dto.DashBoardItemAuditDTO;
import com.globeop.go2.client.dto.DashBoardItemDTO;
import com.globeop.go2.client.dto.DashBoardItemStatusDTO;
import com.globeop.go2.client.dto.DashBoardItemTypeDTO;
import com.globeop.go2.client.dto.DashBoardTypeDTO;
import com.globeop.go2.client.dto.EmatchBlockStatusDTO;
import com.globeop.go2.client.dto.EmatchClearingStatusDTO;
import com.globeop.go2.client.dto.EmatchComparisonFieldDTO;
import com.globeop.go2.client.dto.EmatchDashBoardItemDTO;
import com.globeop.go2.client.dto.EmatchDashBoardStatusMapDTO;
import com.globeop.go2.client.dto.EmatchDashBoardTradeTypeDTO;
import com.globeop.go2.client.dto.EmatchDataMapDTO;
import com.globeop.go2.client.dto.EmatchExportRequestDTO;
import com.globeop.go2.client.dto.EmatchExternalSystemStatusDTO;
import com.globeop.go2.client.dto.EmatchFileDeleteRequestDTO;
import com.globeop.go2.client.dto.EmatchFileUploadRequestDTO;
import com.globeop.go2.client.dto.EmatchMappingTypeDTO;
import com.globeop.go2.client.dto.EmatchRequestStatusDTO;
import com.globeop.go2.client.dto.EmatchTradeBlockDTO;
import com.globeop.go2.client.dto.EmatchTradeStatusRequestDTO;
import com.globeop.go2.client.dto.FundDTO;
import com.globeop.go2.client.dto.NDFIdDTO;
import com.globeop.go2.client.dto.NoteDTO;
import com.globeop.go2.client.dto.TradeAliasDTO;
import com.globeop.go2.client.dto.TradeDTO;
import com.globeop.go2.client.dto.TradeTypeDTO;
import com.globeop.go2.client.dto.TradeTypes;
import com.globeop.go2.client.dto.entitlement.EntitlementRightDTO;
import com.globeop.go2.client.dto.entitlement.EntitlementRoleDTO;
import com.globeop.go2.client.dto.entitlement.EntitlementUserRoleDTO;
import com.globeop.go2.client.dto.query.StoredProcResultSet;
import com.globeop.go2.client.dto.query.StoredProcResults;
import com.globeop.go2.client.dto.reference.CurrencyDTO;
import com.globeop.go2.client.dto.reference.ExternalSystemDTO;
import com.globeop.go2.client.dto.reference.ReferenceDataDTO;
import com.globeop.go2.client.dto.reference.SeverityDTO;
import com.globeop.go2.client.dto.reference.UserDTO;
import com.globeop.go2.client.filters.CentralClearingDTOFilter;
import com.globeop.go2.client.filters.ClientDTOFilter;
import com.globeop.go2.client.filters.CounterpartyDTOFilter;
import com.globeop.go2.client.filters.CurrencyDTOFilter;
import com.globeop.go2.client.filters.DashBoardDTOFilter;
import com.globeop.go2.client.filters.DashBoardItemDTOFilter;
import com.globeop.go2.client.filters.DashBoardItemStatusDTOFilter;
import com.globeop.go2.client.filters.DashBoardItemTypeDTOFilter;
import com.globeop.go2.client.filters.DashBoardTypeDTOFilter;
import com.globeop.go2.client.filters.EmatchBlockStatusDTOFilter;
import com.globeop.go2.client.filters.EmatchClearingStatusDTOFilter;
import com.globeop.go2.client.filters.EmatchComparisonFieldDTOFilter;
import com.globeop.go2.client.filters.EmatchDashBoardItemDTOFilter;
import com.globeop.go2.client.filters.EmatchDashBoardStatusMapDTOFilter;
import com.globeop.go2.client.filters.EmatchDashBoardTradeTypeDTOFilter;
import com.globeop.go2.client.filters.EmatchDataMapDTOFilter;
import com.globeop.go2.client.filters.EmatchExportRequestDTOFilter;
import com.globeop.go2.client.filters.EmatchExternalSystemStatusDTOFilter;
import com.globeop.go2.client.filters.EmatchFileDeleteRequestDTOFilter;
import com.globeop.go2.client.filters.EmatchFileUploadRequestDTOFilter;
import com.globeop.go2.client.filters.EmatchMappingTypeDTOFilter;
import com.globeop.go2.client.filters.EmatchTradeBlockDTOFilter;
import com.globeop.go2.client.filters.EmatchTradeStatusRequestDTOFilter;
import com.globeop.go2.client.filters.ExternalSystemDTOFilter;
import com.globeop.go2.client.filters.FundDTOFilter;
import com.globeop.go2.client.filters.GoConfirmDashBooardItemTradeDTOFilter;
import com.globeop.go2.client.filters.ReferenceDataDTOFilter;
import com.globeop.go2.client.filters.TradeDTOFilter;
import com.globeop.go2.client.filters.TradeTypeDTOFilter;
import com.globeop.go2.client.filters.entitlement.EntitlementRoleDTOFilter;
import com.globeop.go2.client.filters.entitlement.EntitlementUserRoleDTOFilter;
import com.globeop.go2.client.util.DateRange;
import com.globeop.go2.client.util.OrderBy;
import com.globeop.go2.gmt.managerialButtons.dto.ConfDashBoardItemDTO;
import com.globeop.go2.gmt.managerialButtons.dto.ConfMessageLogDTO;
import com.globeop.go2.gmt.managerialButtons.formbean.ManagerialButtonFormBean;
import com.globeop.go2.gmt.trade.controller.TradeSearchHelper;
import com.globeop.go2.gmt.trade.dao.ConfUserFundDAO;
import com.globeop.go2.gmt.trade.dao.DashBoardItemDAO;
import com.globeop.go2.gmt.trade.dto.ConfItemStatusCategoryDTO;
import com.globeop.net.ldap.LdapAuthenticationService;
import com.google.gson.Gson;

/**
 * @author vshilkar
 * @since Feb 23, 2016
 * $Header: /home/cvsd/cvs/root/ematch-web/matching/src/com/globeop/go2/appComponent/service/GMTService.java,v 1.47.2.59.2.1 2017/12/20 13:19:28 vshilkar Exp $
 */
@Service("gmtService")
@PropertySource(value={"classpath:matching.properties"})
public class GMTService extends AbstractService{
	
	private Logger log = LoggerFactory.getLogger(GMTService.class);
	@Autowired
	@Qualifier("clientServiceManager")
	private ClientServiceManager clientServiceManager;
	@Autowired
	@Qualifier("clientServiceManagerReadOnly")
	private ClientServiceManager clientServiceManagerReadOnly;
	@Autowired
	@Qualifier("gmtClientServiceManager")
	private GmtClientServiceManager gmtClientServiceManager;
	private MenuService menuService;
	@Autowired
	@Qualifier("entitlementService")
	private EntitlementService entitlementService;
	@Autowired
	private LdapAuthenticationService ldapAuthenticationService;
	@Autowired
	private CommonSQLExecuterDAO commonSQLExecuterDAO;
	@Autowired
	private ConfObjectQueryDAO confObjectQueryDAO;
	private RestTemplate restTemplate = new RestTemplate(); 

	@Value("${quoppaPDFKey}")
	private String quoppaPDFKey;
	@Value("${g20Url}")
	private String g20Url;
	@Value("${env}")
	private String environ;
	@Value("${externalSites}")
	private String externalSites;
	@Value("${matchingServiceURL}")
	private String matchingServiceURL;

	private Map<String, String> savedSearchmap;
	private Map<String, Integer> referenceDataMap;
	private Map<String, List<DashBoardDTO>> confirmMethodMap = null;
	private Map<String, List<DashBoardItemStatusDTO>> confirmStatusMap = null;
	private Map<String, List<DashBoardItemTypeDTO>> confirmTypeMap = null;
	private Map<String, String> sortingFieldsMap = null;
	private static final String GET_ENTITLED_APPS_FOR_USER = "select distinct et.ShortName as EntitlementType from EntitlementUserRole eur join EntitlementRole er on (eur.EntitlementRoleId = er.EntitlementRoleId) join UserTable u on (u.UserId = eur.UserId) join EntitlementType et on (et.EntitlementTypeId = er.EntitlementTypeId) join EntitlementRoleRight err on (err.EntitlementRoleId = er.EntitlementRoleId) join EntitlementRight eri on (eri.EntitlementRightId = err.EntitlementRightId) where u.ShortName = '" ;
	private static final String SELECT_DTCC_AUDIT_BY_DBID = "select ma.DashBoardItemId,ca.ShortName as Activity,ma.AuditActionId,ma.AuditDateTime,ma.MessageId, ts.ShortName as Status,ma.CreatedDateTime,ma.LastModifiedDateTime,ma.Comments from ConfMessageLogAudit ma join ConfActivity ca on (ma.ActivityId = ca.ActivityId) join ConfTrackerStatus ts on (ma.TrackerStatusId = ts.TrackerStatusId) where ma.DashBoardItemId = ? ORDER BY ma.MessageId DESC, ma.AuditDateTime DESC" ;
	private static final String SELECT_CONF_TRADE_MATCHED= "SELECT  t.* ,ce.ShortName as ExternalSystemStatusName, pt.ShortName as ProductTypeName,f.ShortName as FundShortName, tt.ShortName as TransactionTypeName,cp.ShortName as CounterpartyShortName,tsts.ShortName as TrackerStatus FROM ConfTrade t  JOIN ConfExternalSystemStatus ce ON (t.ExternalSystemStatusId = ce.ExternalSystemStatusId) JOIN Fund f ON (t.FundId = f.FundId)  JOIN ConfProductType pt ON (t.ProductTypeId = pt.ProductTypeId) JOIN ConfTransactionType tt ON (t.TransactionTypeId = tt.TransactionTypeId) JOIN ConfTrackerStatus tsts ON (t.TrackerStatusId=tsts.TrackerStatusId) JOIN Counterparty cp ON (t.CounterpartyId = cp.CounterpartyId) WHERE  t.DashBoardItemId= ?";
	private static final String SELECT_MESSAGEID_BY_DBITEMID = "select MessageId from  ConfMessageLog where DashBoardItemId = ? having MessageId = max(MessageId)";

	private Filter allFilter = initializeDefaultFilterValue();
    
    protected final Filter initializeDefaultFilterValue() {
    	Filter allFilter = new Filter();
		allFilter.setId("");
		allFilter.setName("All");
		allFilter.setSELECTED(false);
        return allFilter;
    }
    
	public GMTService(){
		menuService = MenuService.getMenuService(this);
	}

	public ClientServiceManager getClientServiceManager(){
		return clientServiceManager;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer xxxpropertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * @author vshilkar
	 * @return void
	 * This method executed after GMTService bean initialization and 
	 * loads basic data in Cache.
	 */
	@NonLoggable
	@PostConstruct
	public void initGmtService(){
		MatchingWeb.EXTERNAL_SITES = externalSites;
		loadSavedSearchPageId();
		getTradeTypes();
		getCentralClearing();
		RequestMappingConstants.G20_TRADE_TICKET_URL = "'" + g20Url.trim() + "'";
	}

	@NonLoggable
	public List<FundDTO> findFundByClientId2(Short clientId){
		FundDTOFilter filter = new FundDTOFilter();
		filter.setClientId(Arrays.asList(clientId));
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));

		DateRange range = new DateRange();
		range.setStart(new Date());
		filter.setInactiveDate(range);
		List<FundDTO> funds = clientServiceManager.findFundByFilter(filter, 10000, 0);

		return filterGoOTCFunds(funds);
	}

	@NonLoggable
	public List<FundDTO> filterGoOTCFunds(List<FundDTO> funds) {
		List<FundDTO> result = new ArrayList<FundDTO>();
		Calendar yearAgoTodayCal = new GregorianCalendar();
		yearAgoTodayCal.add(Calendar.YEAR, -1);

		for (FundDTO fund : funds) {
			if (Boolean.TRUE.equals(fund.getTerminationConfirmed()) &&
					fund.getTerminationDate() != null &&
					fund.getTerminationDate().before(yearAgoTodayCal.getTime())) {
				continue;
			}
			result.add(fund);
		}

		return result;
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<ClientDTO> findClient(){
		ClientDTOFilter filter = new ClientDTOFilter();
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));
		filter.setTerminationConfirmed(false);

		return clientServiceManager.findClientByFilter(filter);
	}

	@NonLoggable
	public Boolean isInternalUser(String userName){
		return clientServiceManager.isInternalUser(userName);
	}

	@NonLoggable
	public Boolean isInternalUser(HttpServletRequest request){
		return clientServiceManager.isInternalUser(SessionUtil.getUserName(request));
	}

	@NonLoggable
	public MyEntitlementDTO getMyEntitlement(HttpServletRequest request){
		MyEntitlementDTO myEntitlementDTO = new MyEntitlementDTO();
		String username = SessionUtil.getUserName(request);
		List<EntitlementRightDTO> entitlementRightDTOList = clientServiceManager.findEntitlementRightsForUser(username, "TradeExport");
		for(EntitlementRightDTO entitlementRightDTO : entitlementRightDTOList){
			if("PROCESSTRADES".equalsIgnoreCase(entitlementRightDTO.getShortName())){
				myEntitlementDTO.setProcesstradeRight(true);
			}else if("REQUEST".equalsIgnoreCase(entitlementRightDTO.getShortName())){
				myEntitlementDTO.setRequestRight(true);
			}else if("ALL_CLIENT_ACCESS".equalsIgnoreCase(entitlementRightDTO.getShortName())){
				myEntitlementDTO.setAllclientsRight(true);
			}
		}
		return myEntitlementDTO;
	}

	@NonLoggable
	private UserRights getDocumentationEntitlement(HttpServletRequest request){
		UserRights userRights=new UserRights();
		List<EntitlementRightDTO> entitlementRightDTOList = clientServiceManager.findEntitlementRightsForUser(SessionUtil.getUserName(request), "GoConfirm");
		userRights=setUserRight(entitlementRightDTOList);
		return userRights;
	}


	@NonLoggable
	private String getUserName(HttpServletRequest request) {
		String userName = SessionUtil.getUserName(request);
		if (CommonUtil.isNullOrEmpty(userName)) {
			userName = ldapAuthenticationService.retrieveAuthenticationUsername(request);
		}
		return userName;
	}

	/**
	 * Get {@link UserDTO} from {@link HttpServletRequest}
	 */
	@NonLoggable
	private UserDTO getUserDTO(HttpServletRequest request) {
		UserSessionDTO userSessionDTO = SessionUtil.getUserSessionDTO(request);
		UserDTO userDTO=null;
		if(userSessionDTO!=null && userSessionDTO.getUserDTO()!=null){
			userDTO=userSessionDTO.getUserDTO();
		}else{
			userDTO=getUserByName(getUserName(request));
		}
		return userDTO;
	}

	@NonLoggable
	public List<FundDTO> findGoOTCFundDTOList(List<String> fundNameList) {
		FundDTOFilter filter = new FundDTOFilter();
		filter.setShortName(fundNameList);

		DateRange range = new DateRange();
		range.setStart(new Date());
		filter.setInactiveDate(range);

		List<FundDTO> funds = clientServiceManager.findFundByFilter(filter);

		return filterGoOTCFunds(funds);
	}

	@NonLoggable
	public Menu getMenu(HttpServletRequest request){
		return menuService.getMenu(request);
	}

	@NonLoggable
	public boolean isSpecialUser(HttpServletRequest request){
		boolean found = false;

		EntitlementUserRoleDTOFilter filter1 = new EntitlementUserRoleDTOFilter();
		filter1.setUserShortName(Arrays.asList(SessionUtil.getUserName(request)));
		List<EntitlementUserRoleDTO> userRoles = clientServiceManager.findEntitlementUserRoleByFilter(filter1);
		List<Integer> roleIdList = new ArrayList<Integer>();
		for(EntitlementUserRoleDTO dto : userRoles)
		{
			roleIdList.add(dto.getEntitlementRoleId());
		}
		EntitlementRoleDTOFilter filter2 = new EntitlementRoleDTOFilter();
		filter2.setEntitlementRoleId(roleIdList);
		filter2.setEntitlementTypeShortName(Arrays.asList("TradeExport"));
		List<EntitlementRoleDTO> roles = clientServiceManager.findEntitlementRoleByFilter(filter2);
		for(EntitlementRoleDTO dto : roles){
			if("SPECIAL_EXTERNAL_USER".equalsIgnoreCase(dto.getShortName())){
				found = true;
				break;
			}
		}

		return found;
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<ClientDTO> getAuthorizedClients(HttpServletRequest request){
		return entitlementService.getAuthorizedClients(request);
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<ExternalSystemDTO> getExternalSystem(){
		List<ExternalSystemDTO> list = new ArrayList<ExternalSystemDTO>();
		String sql =    "select es.ExternalSystemId, es.ShortName, es.Description " +
				"from ExternalSystem es, ExternalSystemType est " +
				"where es.ExternalSystemTypeId = est.ExternalSystemTypeId and " +
				"est.ShortName = 'AFFIRMATIONSYSTEM' and " +
				"es.InactiveDate is not null " +
				"order by es.ShortName asc";

		List list2 = gmtClientServiceManager.getPlatform1(sql);
		Iterator iterator = list2.iterator();
		while(iterator.hasNext()){
			List resultSet = (List)iterator.next();
			Integer externalSystemId = (Integer)resultSet.get(0);
			String externalSystemShortName = ((String)resultSet.get(1)).trim();
			String externalSystemDescription = ((String)resultSet.get(2)).trim();
			ExternalSystemDTO dto = new ExternalSystemDTO();
			dto.setId(externalSystemId);
			dto.setShortName(externalSystemShortName);
			dto.setDescription(externalSystemDescription);
			list.add(dto);
		}
		return list;
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<ExternalSystemDTO> getExternalSystemSwift(){
		List<ExternalSystemDTO> list = new ArrayList<ExternalSystemDTO>();
		String sql =    "select es.ExternalSystemId, es.ShortName, es.Description " +
				"from ExternalSystem es, ExternalSystemType est " +
				"where es.ExternalSystemTypeId = est.ExternalSystemTypeId and " +
				"est.ShortName = 'AFFIRMATIONSYSTEM' and " +
				"es.InactiveDate is not null and " +
				"es.ShortName = 'SWIFT' " +
				"order by es.ShortName asc";

		List list2 = gmtClientServiceManager.getPlatform1(sql);
		Iterator iterator = list2.iterator();
		while(iterator.hasNext()){
			List resultSet = (List)iterator.next();
			Integer externalSystemId = (Integer)resultSet.get(0);
			String externalSystemShortName = ((String)resultSet.get(1)).trim();
			ExternalSystemDTO dto = new ExternalSystemDTO();
			dto.setId(externalSystemId);
			dto.setShortName(externalSystemShortName);
			list.add(dto);
		}
		return list;
	}

	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<String> getEmatchUnExecutedStatus(){
		List<String> matchStatusIds = loadUnExecutedStatus("TradeExport.Ematch", "EMATCH_UNEXECUTED");
		return matchStatusIds;
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<String> getConfirmsUnExecutedStatus(){
		List<String> confirmsStatusIds = loadUnExecutedStatus("ISDA Confirms", "UNEXECUTED");
		return confirmsStatusIds;
	}

	public List<String> loadUnExecutedStatus(String dashBoardType, String statusCategory){
		List<String> list =new ArrayList<String>();
		try {
			String UNEXECUTEDSTATUSIDS = " SELECT ic.DashBoardItemStatusId FROM Conf_ItemStatusCategory ic, Conf_StatusCategory sc, "
					+ " DashBoardType dt WHERE ic.StatusCategoryId = sc.StatusCategoryId AND "
					+ " sc.DashBoardTypeId = dt.DashBoardTypeId "
					+ " AND dt.ShortName = '"+dashBoardType+"'"
					+ " AND sc.ShortName = '"+statusCategory+"'";
			/*List<Object> resultList=CommonSQLExecuterDAO.getInstance().executeSQL(UNEXECUTEDSTATUSIDS);
			if(CommonUtil.isNullOrEmpty(resultList)){
				return list;
			}
			
			for(int i=0; i<resultList.size(); i++){
				String dbiStatusId=String.valueOf((Short) resultList.get(i));
				list.add(dbiStatusId);
			 }*/
			
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + UNEXECUTEDSTATUSIDS, ListDTO.class);
			if(!CommonUtil.isNull(listDTO)){
				list = listDTO.getStringList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	
	public ExternalSystemDTO getExternalSystemById(Integer externalSystemId){
		return clientServiceManager.findExternalSystemById(externalSystemId);
	}

	public ExternalSystemDTO getExternalSystemByShortName(String shortName){
		return clientServiceManager.findExternalSystemByShortName(shortName);
	}

	public List<ExternalSystemDTO> getExternalSystemByFilter(ExternalSystemDTOFilter filter){
		return clientServiceManager.findExternalSystemByFilter(filter);
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<CounterpartyDTO> getCounterparty(){
		CounterpartyDTOFilter cptyDTOfilter = new CounterpartyDTOFilter();
		Calendar calendar = new GregorianCalendar();
		calendar.set(9999, 11, 31);
		Date startDate = new Date();
		Date endDate = calendar.getTime();
		DateRange dateRange = new DateRange(startDate, endDate);
		cptyDTOfilter.setInactiveDate(dateRange);
		cptyDTOfilter.addOrderBy(new OrderBy("shortName", ASCENDING));
		return clientServiceManager.findCounterpartyByFilter(cptyDTOfilter);
	}

	public CounterpartyDTO getCounterpartyById(Integer counterpartyId){
		return clientServiceManager.findCounterpartyById(counterpartyId);
	}

	public CounterpartyDTO getCounterpartyByShortName(String cpty){
		return clientServiceManager.findCounterpartyByShortName(cpty);
	}

	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public TradeTypes findGoOTCTradeTypes(){
		return clientServiceManager.findGoOTCTradeTypes();
	}
	
	public List<TradeTypeDTO> findTradeTypeByFilter(TradeTypeDTOFilter filter){
		return clientServiceManager.findTradeTypeByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<CurrencyDTO> getCurrency(){
		CurrencyDTOFilter filter = new CurrencyDTOFilter();
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));
		return clientServiceManager.findCurrencyByFilter(filter);
	}

	public CurrencyDTO getCurrencyById(String currencyId){
		return clientServiceManager.findCurrencyById(currencyId);
	}

	public ClientDTO getClientByClientId(Short clientId){
		return clientServiceManager.findClientById(clientId);
	}

	public ClientDTO getClientByShortname(String shortname){
		return clientServiceManager.findClientByShortName(shortname);
	}

	public ClientDTO getClientByFundId(Integer fundId){
		FundDTO fundDTO = getFundById(fundId);
		return getClientByClientId(fundDTO.getClientId());
	}

	@Cacheable(key="#root.methodName",value=Framework.COMMON_CACHE)
	public List getGoMatchEligibleClients(){
		String sp_eec = "EmatchEligibleClients";
		List<String> inputParams_eec = new ArrayList<String>();
		Map<Short, ClientDTO> master_client_map = new HashMap<Short, ClientDTO>();
		List clientList = new ArrayList();

		try {
			//StoredProcResults spResults = executeStoredProc(sp_eec, inputParams_eec.toArray(), new String[]{});
			StoredProcResults spResults = executeStoredProcReadOnly(sp_eec, inputParams_eec.toArray(), new String[]{});

			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			for(StoredProcResultSet spResultSet : spResultSets)
			{
				if(spResultSet.getColumnCount()>1)
				{
					String client = "";
					Short clientId;

					for(int i=1; i<=spResultSet.getRowCount() ; i++)
					{
						if(spResultSet.getObject(i, 2)!=null){client = ((String)spResultSet.getObject(i, 2)).trim();}
						ClientDTO clientDTO = getClientByShortname(client);
						if(clientDTO==null){continue;}
						clientId = clientDTO.getId();

						if(!master_client_map.containsKey(clientId)){master_client_map.put(clientId, clientDTO);}
					}
				}
			}

			clientList = Arrays.asList(master_client_map.values().toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return clientList; 
	}

	@Cacheable(key="#root.methodName",value=Framework.COMMON_CACHE)
	public BaseDTO getGoMatchEligibleClientsMap(){
		BaseDTO baseDTO = new BaseDTO();
		String sp_eec = "EmatchEligibleClients";
		List<String> inputParams_eec = new ArrayList<String>();
		Map<Integer, List<Short>> master_client_map = new HashMap<Integer, List<Short>>();

		try {
			//StoredProcResults spResults = executeStoredProc(sp_eec, inputParams_eec.toArray(), new String[]{});
			StoredProcResults spResults = executeStoredProcReadOnly(sp_eec, inputParams_eec.toArray(), new String[]{});

			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			for(StoredProcResultSet spResultSet : spResultSets)
			{
				if(spResultSet.getColumnCount()>1)
				{
					String externalSystem = "", client = "";
					Integer externalSystemId;
					Short clientId;

					for(int i=1; i<=spResultSet.getRowCount() ; i++)
					{
						try {
							if (spResultSet.getObject(i, 1) != null) {
								externalSystem = ((String) spResultSet.getObject(i, 1)).trim();
							}
							if (spResultSet.getObject(i, 2) != null) {
								client = ((String) spResultSet.getObject(i, 2)).trim();
							}
							externalSystemId = getExternalSystemByShortName(externalSystem).getId();
							ClientDTO clientDTO = getClientByShortname(client);
							if (clientDTO == null) {
								continue;
							}
							clientId = clientDTO.getId();

							if (!master_client_map.containsKey(externalSystemId)) {
								master_client_map.put(externalSystemId, new ArrayList<Short>());
							}

							List<Short> clientList = master_client_map.get(externalSystemId);
							if (!clientList.contains(clientId)) {
								clientList.add(clientId);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			baseDTO.setObjectMap(master_client_map);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return baseDTO; 
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<Filter> getTradeTypes(){
		List<Filter> tradeTypeList = new ArrayList<Filter>();
		TradeTypes tt = findGoOTCTradeTypes();

		/*For putting All from java*/
		Filter allTypeFilter = new Filter();
		allTypeFilter.setId("");
		allTypeFilter.setName("All");
		allTypeFilter.setSELECTED(false);
		tradeTypeList.add(allTypeFilter);
		/**/
		
		Filter filter = new Filter();
		filter.setId(TradeTypeId.ALL_OTC);
		filter.setName("-- ALL OTC TRADE TYPES --");
		filter.setSELECTED(false);
		tradeTypeList.add(filter);

		for (TradeTypeDTO typeDTO : tt.getOtcTradeTypes()) {
			Filter ft = new Filter();
			ft.setId(typeDTO.getTradeTypeId().toString());
			ft.setName(typeDTO.getDescription());
			ft.setSELECTED(false);
			tradeTypeList.add(ft);

			if("CREDIT_DEFAULT_SWAP".equalsIgnoreCase(typeDTO.getShortName())){
				for (CustomTradeTypeDTO dto : findCRESubTradeType()) {
					Filter creFt = new Filter();
					creFt.setId(typeDTO.getTradeTypeId() + "_" + dto.getId());
					creFt.setName("-"+dto.getDescription());
					creFt.setSELECTED(false);
					tradeTypeList.add(creFt);
				}
			}
		}


		filter = new Filter();
		filter.setId(TradeTypeId.ALL_NON_OTC);
		filter.setName("-- ALL NON-OTC TRADE TYPES --");
		filter.setSELECTED(false);
		tradeTypeList.add(filter);

		// Non-OTC - start
		List<TradeTypeDTO> nonOTC = tt.getNonOtcTradeTypes();
		for(TradeTypeDTO dto : nonOTC){
			if(StaticValuesUtil.getRestrictedNonOTCTradeTypes().contains(dto.getDescription())){
				Filter flt = new Filter();
				flt.setId(dto.getTradeTypeId().toString());
				flt.setName(dto.getDescription());
				flt.setSELECTED(false);
				tradeTypeList.add(flt);
			}

			if("FORWARD_DEALS".equalsIgnoreCase(dto.getShortName())){
				List<NDFIdDTO> nDFId = findNDF();
				for(NDFIdDTO ndfIdDTO : nDFId){
					Filter ndfFt = new Filter();
					ndfFt.setId(dto.getTradeTypeId()+"_"+ndfIdDTO.getId());
					ndfFt.setName("-"+ndfIdDTO.getDescription());
					ndfFt.setSELECTED(false);
					tradeTypeList.add(ndfFt);
				}
			}
		}


		return tradeTypeList;
	}
	
	public List<Filter> getTradeTypes(UserType userType){
		List<Filter> tradeTypeList = new ArrayList<Filter>();
		TradeTypes tt = findGoOTCTradeTypes();

		Filter filter = new Filter();
		if(UserType.EXTERNAL_OTC_USER.equals(userType)
				|| UserType.INTERNAL_OTC_USER.equals(userType)){
			filter.setId(TradeTypeId.ALL_OTC);
			filter.setName("-- ALL OTC TRADE TYPES --");
			filter.setSELECTED(false);
			tradeTypeList.add(filter);

			for (TradeTypeDTO typeDTO : tt.getOtcTradeTypes()) {
				Filter ft = new Filter();
				ft.setId(typeDTO.getTradeTypeId().toString());
				ft.setName(typeDTO.getDescription());
				ft.setSELECTED(false);
				tradeTypeList.add(ft);

				if("CREDIT_DEFAULT_SWAP".equalsIgnoreCase(typeDTO.getShortName())){
					for (CustomTradeTypeDTO dto : findCRESubTradeType()) {
						Filter creFt = new Filter();
						creFt.setId(typeDTO.getTradeTypeId() + "_" + dto.getId());
						creFt.setName("-"+dto.getDescription());
						creFt.setSELECTED(false);
						tradeTypeList.add(creFt);
					}
				}
			}

		} else if(UserType.EXTERNAL_NONOTC_USER.equals(userType)
				|| UserType.INTERNAL_NONOTC_USER.equals(userType)){
			filter = new Filter();
			filter.setId(TradeTypeId.ALL_NON_OTC);
			filter.setName("-- ALL NON-OTC TRADE TYPES --");
			filter.setSELECTED(false);
			tradeTypeList.add(filter);

			// Non-OTC - start
			List<TradeTypeDTO> nonOTC = tt.getNonOtcTradeTypes();
			for(TradeTypeDTO dto : nonOTC){
				if(StaticValuesUtil.getRestrictedNonOTCTradeTypes().contains(dto.getDescription())){
					Filter flt = new Filter();
					flt.setId(dto.getTradeTypeId().toString());
					flt.setName(dto.getDescription());
					flt.setSELECTED(false);
					tradeTypeList.add(flt);
				}

				if("FORWARD_DEALS".equalsIgnoreCase(dto.getShortName())){
					List<NDFIdDTO> nDFId = findNDF();
					for(NDFIdDTO ndfIdDTO : nDFId){
						Filter ndfFt = new Filter();
						ndfFt.setId(dto.getTradeTypeId()+"_"+ndfIdDTO.getId());
						ndfFt.setName("-"+ndfIdDTO.getDescription());
						ndfFt.setSELECTED(false);
						tradeTypeList.add(ndfFt);
					}
				}
			}
		}else{
			filter = new Filter();
			filter.setId(TradeTypeId.ALL_OTC);
			filter.setName("-- ALL OTC TRADE TYPES --");
			filter.setSELECTED(false);
			tradeTypeList.add(filter);

			for (TradeTypeDTO typeDTO : tt.getOtcTradeTypes()) {
				Filter ft = new Filter();
				ft.setId(typeDTO.getTradeTypeId().toString());
				ft.setName(typeDTO.getDescription());
				ft.setSELECTED(false);
				tradeTypeList.add(ft);

				if("CREDIT_DEFAULT_SWAP".equalsIgnoreCase(typeDTO.getShortName())){
					for (CustomTradeTypeDTO dto : findCRESubTradeType()) {
						Filter creFt = new Filter();
						creFt.setId(typeDTO.getTradeTypeId() + "_" + dto.getId());
						creFt.setName("-"+dto.getDescription());
						creFt.setSELECTED(false);
						tradeTypeList.add(creFt);
					}
				}
			}


			filter = new Filter();
			filter.setId(TradeTypeId.ALL_NON_OTC);
			filter.setName("-- ALL NON-OTC TRADE TYPES --");
			filter.setSELECTED(false);
			tradeTypeList.add(filter);

			// Non-OTC - start
			List<TradeTypeDTO> nonOTC = tt.getNonOtcTradeTypes();
			for(TradeTypeDTO dto : nonOTC){
				if(StaticValuesUtil.getRestrictedNonOTCTradeTypes().contains(dto.getDescription())){
					Filter flt = new Filter();
					flt.setId(dto.getTradeTypeId().toString());
					flt.setName(dto.getDescription());
					flt.setSELECTED(false);
					tradeTypeList.add(flt);
				}

				if("FORWARD_DEALS".equalsIgnoreCase(dto.getShortName())){
					List<NDFIdDTO> nDFId = findNDF();
					for(NDFIdDTO ndfIdDTO : nDFId){
						Filter ndfFt = new Filter();
						ndfFt.setId(dto.getTradeTypeId()+"_"+ndfIdDTO.getId());
						ndfFt.setName("-"+ndfIdDTO.getDescription());
						ndfFt.setSELECTED(false);
						tradeTypeList.add(ndfFt);
					}
				}
			}
		}

		return tradeTypeList;
	}

	public List<TradeTypeDTO> getTradeTypeByPlatformIds(List<Integer> platformIdList){
		List<DashBoardDTO> dashboardList = getDashBoardByExternalSystem(platformIdList);
		List<Short> dashboardIdList = new ArrayList<Short>();
		for(DashBoardDTO dto : dashboardList){
			dashboardIdList.add(dto.getId());
		}
		if(dashboardIdList.size()==0){
			return null;
		}else{
			return getTradeTypeByDashBoardIds(dashboardIdList);
		}
	}

	public List<TradeTypeDTO> getTradeTypeByDashBoardIds(List dashboardList){
		List<TradeTypeDTO> tradeTypeList = new ArrayList<TradeTypeDTO>();
		EmatchDashBoardTradeTypeDTOFilter tepDashBoardTradeTypeDTOFilter = new EmatchDashBoardTradeTypeDTOFilter();
		tepDashBoardTradeTypeDTOFilter.setDashBoardId(dashboardList);
		List<EmatchDashBoardTradeTypeDTO> result = gmtClientServiceManager.findTepDashBoardTradeTypeByFilter(tepDashBoardTradeTypeDTOFilter);

		if(result==null || result.size()==0){
			return null;
		}else{
			for(EmatchDashBoardTradeTypeDTO dto : result){
				Short tradeTypeId = dto.getTradeTypeId();
				tradeTypeList.add(getTradeTypeById(tradeTypeId));
			}
			return tradeTypeList;
		}
	}

	public TradeTypeDTO getTradeTypeById(Short tradeTypeId){
		return clientServiceManager.findTradeTypeById(tradeTypeId);
	}

	public List<DashBoardDTO> getDashBoardByExternalSystem(List<Integer> externalSystemIdList){
		DashBoardDTOFilter dashBoardDTOFilter = new DashBoardDTOFilter();
		dashBoardDTOFilter.setExternalsystemId(externalSystemIdList);
		dashBoardDTOFilter.addOrderBy(new OrderBy("shortName", ASCENDING));
		return clientServiceManager.findDashBoardByFilter(dashBoardDTOFilter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardItemStatusDTO> getDashBoardItemStatus(){
		DashBoardItemStatusDTOFilter filter = new DashBoardItemStatusDTOFilter();
		filter.setDashBoardTypeShortName(Arrays.asList("TradeExport.Ematch"));
		return gmtClientServiceManager.findDashBoardItemStatusByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<TradeTypeDTO> findOTCTradeType(){
		TradeTypeDTOFilter filter = new TradeTypeDTOFilter();
		filter.setOtcFlag(true);
		filter.addOrderBy(new OrderBy("description", ASCENDING));
		return clientServiceManager.findTradeTypeByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<TradeTypeDTO> findNonOTCTradeType(){
		TradeTypeDTOFilter filter = new TradeTypeDTOFilter();
		filter.setOtcFlag(false);
		filter.addOrderBy(new OrderBy("description", ASCENDING));
		List<TradeTypeDTO> dtoList = clientServiceManager.findTradeTypeByFilter(filter);

		for(TradeTypeDTO dto : dtoList){
			if("PRODUCT".equals(CommonUtil.trim(dto.getShortName()))){
				dtoList.remove(dto);
			}
		}
		return dtoList;
	}

	/**
	 * Get OTC product map
	 */
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, String> getOtcProductMap() {
		Map<String, String> OtcProductMap=new HashMap<String, String>();
		try {
			List<TradeTypeDTO> OTCTradeType=findOTCTradeType();
			if(CommonUtil.isNullOrEmpty(OTCTradeType)){
				return OtcProductMap;
			}
			for(int i=0; i<OTCTradeType.size(); i++){
				TradeTypeDTO  tt = OTCTradeType.get(i);
				String key=String.valueOf(tt.getTradeTypeId());
				String value=tt.getShortName();
				OtcProductMap.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return OtcProductMap;
	}

	/**
	 * Get Non-OTC product map
	 */
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, String> getNonOtcProductMap() {
		Map<String, String> nonOtcProductMap=new HashMap<String, String>();
		try {
			List<TradeTypeDTO> nonOTCTradeType=findNonOTCTradeType();
			if(CommonUtil.isNullOrEmpty(nonOTCTradeType)){
				return nonOtcProductMap;
			}
			for(int i=0; i<nonOTCTradeType.size(); i++){
				TradeTypeDTO  tt = nonOTCTradeType.get(i);
				String key=String.valueOf(tt.getTradeTypeId());
				String value=tt.getShortName();
				nonOtcProductMap.put(key, value);
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getNonOtcProductMap", e);
		}
		return nonOtcProductMap;
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, ArrayList<String>> loadConfStatusCategories() {
		Map<String, ArrayList<String>>  confStatusCategoriesMap = new HashMap<String, ArrayList<String>>();
		try {
			List<ConfItemStatusCategoryDTO> list=DashBoardItemDAO.getInstance().loadConfStatusCategories();
			if (list!=null && list.size()>0){
				for (ConfItemStatusCategoryDTO dto : list){
					if (confStatusCategoriesMap.get(dto.getStatusCategoryName())!=null){
						confStatusCategoriesMap.get(dto.getStatusCategoryName()).add(String.valueOf(dto.getDashBoardItemStatusId()));
					}else{
						ArrayList<String> a = new ArrayList<String>();
						a.add(String.valueOf(dto.getDashBoardItemStatusId()));
						confStatusCategoriesMap.put(dto.getStatusCategoryName().trim(), a);
					}
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getDefaultColumnMap", e);
		}
		return confStatusCategoriesMap;
	}
	
	public List<CustomTradeTypeDTO> findCRESubTradeType(){
		return Arrays.asList(CustomTradeTypeDTO.getValues());
	}

	public List<NDFIdDTO> findNDF(){
		return Arrays.asList(NDFIdDTO.getValues());
	}

	public FundDTO getFundById(Integer fundId){
		return clientServiceManager.findFundById(fundId);
	}
	
	public List<FundDTO> findFundByFilter(FundDTOFilter dtoFilter){
		return clientServiceManager.findFundByFilter(dtoFilter);
	}

	public Boolean isAuthorizedForClient(Short clientId, HttpServletRequest request) {
		Assert.notNull(clientId);
		Assert.notNull(request);

		List<ClientDTO> authClients = SessionUtil.getAuthorizedClientList(request);

		for (ClientDTO client : authClients) {
			if (clientId.equals(client.getId())) {
				return true;
			}
		}

		return false;
	}

	public List<FundDTO> getFundByClientId(List<Short> clientId, HttpServletRequest request){
		//WebContext ctx = WebContextFactory.get();
		//HttpServletRequest request = ctx.getHttpServletRequest();
		UserSessionDTO sessionDTO = SessionUtil.getUserSessionDTO(request);
		List<FundDTO> list = new ArrayList<FundDTO>();
		Map<String, List<FundDTO>> fundMap = sessionDTO.getClientFundMap();
		for(Short id : clientId){
			if(fundMap!= null && fundMap.containsKey(id.toString())){
				list.addAll(fundMap.get(id.toString()));
			}else {
				List<FundDTO> l = entitlementService.get_authorized_funds(id, request);
				list.addAll(l);
				if(fundMap==null){
					fundMap = new HashMap<String, List<FundDTO>>();
				}
				fundMap.put(id.toString(), l);
			}
		}
		sessionDTO.setClientFundMap(fundMap);
		SessionUtil.setUserSessionDTO(request, sessionDTO);
		return list;
	}

	@Cacheable(key="#clientIdList", value=Framework.COMMON_CACHE)
	public List<CounterpartyDTO> getCounterpartyByClientId(List<Short> clientIdList){
		CounterpartyDTOFilter filter = new CounterpartyDTOFilter();
		filter.setClientId(clientIdList);
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));

		return clientServiceManager.findCounterpartyByFilter(filter);
	}

	public List<EmatchDataMapDTO> getEmatchDataMap(EmatchDataMapDTOFilter filter){
		return gmtClientServiceManager.findEmatchDataMapByFilter(filter);
	}

	public List<EmatchMappingTypeDTO> getEmatchMappingTypeByFilter(EmatchMappingTypeDTOFilter filter){
		return gmtClientServiceManager.findEmatchMappingTypeByFilter(filter);
	}

	public UserDTO getUserByName(String shortName){
		return clientServiceManager.findActiveUserByShortName(shortName);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<UserDTO> getActiveUsers(){
		List<UserDTO> userDTOList = clientServiceManager.findActiveUsers();
		Collections.sort(userDTOList, new UserComparator());
		return userDTOList;
	}

	public UserDTO getUserById(Integer userId){
		return clientServiceManager.findUserById(userId);
	}

	public String get_authenticated_user_name(HttpServletRequest request){
		return entitlementService.get_authenticated_user_name(request);
	}

	public EmatchDataMapDTO insertEmatchDataMap(EmatchDataMapDTO dto){
		return gmtClientServiceManager.persistEmatchDataMap(dto);
	}

	public void removeEmatchDataMap(Integer mappingId){
		gmtClientServiceManager.removeEmatchDataMapById(mappingId);
	}

	public EmatchDataMapDTO getEmatchDataMapById(Integer id){
		return gmtClientServiceManager.findEmatchDataMapById(id);
	}

	public String getClientsJSON(HttpServletRequest request) {
		List<Filter> list = new ArrayList<Filter>();
		List<ClientDTO> clientList = SessionUtil.getAuthorizedClientList(request);
		Filter filter = null;
		for (ClientDTO clientDTO : clientList) {
			filter = new Filter();
			filter.setId(clientDTO.getId().toString());
			filter.setName(clientDTO.getShortName());
			filter.setSELECTED(false);
			list.add(filter);
		}
		return new Gson().toJson(list);
	}

	public List<ApplicationNameDTO> getGoConfirmStatus(String className){
		List<ApplicationNameDTO> list = new ArrayList<ApplicationNameDTO>();
		String sql =    "select dbis.DashBoardItemStatusId, rd.ShortName as RefData " +
				"from EmatchDashBoardStatusMap emap " +
				"join DashBoardItemStatus dbis on (emap.DashBoardItemStatusId = dbis.DashBoardItemStatusId) " +
				"join ReferenceData rd on (emap.ReferenceDataId = rd.ReferenceDataId) " +
				"where dbis.DashBoardTypeId = 4 and " +
				"rd.ClassName = '" + className + "' " +
				"order by rd.ShortName";        

		List results = gmtClientServiceManager.getGoConfirmStatus(sql);
		Iterator iterator = results.iterator();
		while(iterator.hasNext()){
			List result = (List)iterator.next();
			Short dashBoardItemStatusId = (Short)result.get(0);
			String refData = ((String)result.get(1)).trim();

			ApplicationNameDTO dto = new ApplicationNameDTO();
			dto.setDashBoardTypeId(dashBoardItemStatusId);
			dto.setDisplayName(refData);

			list.add(dto);
		}
		return list;
	}

	@Cacheable(key="#platformIds", value=Framework.COMMON_CACHE)
	public List<GeneralDTO> getPlatformDashBoardItemStatus(String platformIds){
		List<GeneralDTO> list = new ArrayList<GeneralDTO>();
		String sql =    "select distinct dbis.DashBoardItemStatusId, dbis.Description as RefData " +
				"from EmatchDashBoardStatusMap emap " +
				"join DashBoardItemStatus dbis on (emap.DashBoardItemStatusId = dbis.DashBoardItemStatusId) " +
				"join ReferenceData rd on (emap.ReferenceDataId = rd.ReferenceDataId) " +
				"join DashBoard db on (emap.DashBoardId = db.DashBoardId) " +
				"join ExternalSystem es on (db.ExternalSystemId = es.ExternalSystemId) " +
				"join DashBoardType dbt on (db.DashBoardTypeId = dbt.DashBoardTypeId) " +
				"where es.ExternalSystemId in(" + platformIds + ") and " +
				"dbt.ShortName = 'TradeExport.Ematch' and " +
				"rd.ClassName = 'EmatchDashBoardReportingStatus' and " +
				"rd.ShortName in ('ERROR', 'UNMATCHED', 'MISMATCHED')";

		List results = gmtClientServiceManager.getGoConfirmStatus(sql);
		Iterator iterator = results.iterator();
		while(iterator.hasNext()){
			List result = (List)iterator.next();
			Short dashBoardItemStatusId = (Short)result.get(0);
			String refData = ((String)result.get(1)).trim();

			GeneralDTO dto = new GeneralDTO();
			dto.setId(dashBoardItemStatusId);
			dto.setDescription(refData);

			list.add(dto);
		}
		return list;
	}

	public List<GeneralDTO> getPlatformDashBoardItemStatus(String platformIds, String status){
		List<GeneralDTO> list = new ArrayList<GeneralDTO>();

		String sql =    "select distinct dbis.DashBoardItemStatusId, dbis.Description as RefData " +
				"from EmatchDashBoardStatusMap emap " +
				"join DashBoardItemStatus dbis on (emap.DashBoardItemStatusId = dbis.DashBoardItemStatusId) " +
				"join ReferenceData rd on (emap.ReferenceDataId = rd.ReferenceDataId) " +
				"join DashBoard db on (emap.DashBoardId = db.DashBoardId) " +
				"join ExternalSystem es on (db.ExternalSystemId = es.ExternalSystemId) " +
				"join DashBoardType dbt on (db.DashBoardTypeId = dbt.DashBoardTypeId) " +
				"where es.ExternalSystemId in(" + platformIds + ") and " +
				"dbt.ShortName = 'TradeExport.Ematch' and " +
				"rd.ClassName = 'EmatchDashBoardReportingStatus' and " +
				"rd.ShortName = '" + status + "'";

		List results = gmtClientServiceManager.getGoConfirmStatus(sql);
		Iterator iterator = results.iterator();
		while(iterator.hasNext())
		{
			List result = (List)iterator.next();
			Short dashBoardItemStatusId = (Short)result.get(0);
			String refData = ((String)result.get(1)).trim();

			GeneralDTO dto = new GeneralDTO();
			dto.setId(dashBoardItemStatusId);
			dto.setDescription(refData);

			list.add(dto);
		}

		return list;
	}

	public List<Integer> getProductGroupTradeType(String productGroup, String mappingType){
		String sql =    "select tt.TradeTypeId " +
				"from EmatchDataMap dm " +
				"join EmatchMappingType mt on(dm.MappingTypeId = mt.MappingTypeId) " +
				"join TradeType tt on(dm.InternalValue = tt.Code) " +
				"where mt.ShortName = '" + mappingType + "' and " +
				"dm.ExternalValue = '" + productGroup + "'";

		return gmtClientServiceManager.getProductGroupTradeType(sql);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, Map<String, List<DefaultColumnsDTO>>> getDefaultColumnMap(){
		Map<String, Map<String, List<DefaultColumnsDTO>>> default_column_map = new HashMap<String, Map<String, List<DefaultColumnsDTO>>>();
		try {
			List<String> inputParams_dc = new ArrayList<String>();
			//StoredProcResults spResults = executeStoredProc(Database.SP_EMATCHDEFAULTUSERTRADECOLUMN, inputParams_dc.toArray(), new String[]{});
			StoredProcResults spResults = executeStoredProcReadOnly(Database.SP_EMATCHDEFAULTUSERTRADECOLUMN, inputParams_dc.toArray(), new String[]{});
			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			for(StoredProcResultSet spResultSet : spResultSets){
				if(spResultSet.getColumnCount()>1){
					for(int i=1; i<=spResultSet.getRowCount() ; i++){
						String externalSystem = "", status = "", column = "", dtoProperty = "", dataType = "", dataIndex = "", dataProperty = "", dataWidth = "", dataAlign = "";
						Integer precedence = null;

						if(spResultSet.getObject(i, 1)!=null){
							externalSystem = ((String)spResultSet.getObject(i, 1)).trim();
						}
						if(spResultSet.getObject(i, 2)!=null){
							status = ((String)spResultSet.getObject(i, 2)).trim();
						}
						if(spResultSet.getObject(i, 3)!=null){
							precedence = (Integer)spResultSet.getObject(i, 3);
						}
						if(spResultSet.getObject(i, 4)!=null){
							column = ((String)spResultSet.getObject(i, 4)).trim();
						}
						if(spResultSet.getObject(i, 5)!=null){
							dtoProperty = (String)spResultSet.getObject(i, 5);
						}

						dtoProperty = CommonUtil.trim(dtoProperty);
						if(!"".equals(dtoProperty)){
							Integer colonIndex = dtoProperty.indexOf(":");
							Integer pipeIndex = dtoProperty.indexOf("|");
							Integer exclaimIndex = dtoProperty.indexOf("!");
							Integer caretIndex = dtoProperty.indexOf("^");
							dataType = dtoProperty.substring(0, colonIndex);
							dataIndex = dtoProperty.substring(colonIndex+1, pipeIndex);
							dataProperty = dtoProperty.substring(pipeIndex+1, exclaimIndex);
							dataWidth = dtoProperty.substring(exclaimIndex+1, caretIndex);
							dataAlign = dtoProperty.substring(caretIndex+1);
						}

						if(!default_column_map.containsKey(externalSystem)){
							default_column_map.put(externalSystem, new HashMap<String, List<DefaultColumnsDTO>>());
						}

						Map<String, List<DefaultColumnsDTO>> statusHash = default_column_map.get(externalSystem);
						if(!statusHash.containsKey(status)){
							statusHash.put(status, new ArrayList<DefaultColumnsDTO>());
						}

						List<DefaultColumnsDTO> columnList = statusHash.get(status);
						DefaultColumnsDTO dto = new DefaultColumnsDTO();
						dto.setColumnName(column);
						dto.setPrecedence(precedence);
						dto.setDataType(dataType);
						if (!CommonUtil.isNullOrEmpty(dataIndex)) {
							dto.setDataIndex(Integer.valueOf(dataIndex));	
						}
						dto.setDataProperty(dataProperty);
						dto.setDataWidth(dataWidth);
						dto.setDataAlign(dataAlign);
						columnList.add(dto);
					}
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getDefaultColumnMap", e);
		}
		return default_column_map;    	
	}

	public List<CounterpartyDTO> getCounterpartyByFundList(List<Integer> fundIdList){
		CounterpartyDTOFilter filter = new CounterpartyDTOFilter();
		filter.setFundId(fundIdList);
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));
		return clientServiceManager.findCounterpartyByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<CentralClearingDTO> getCentralClearing(){
		CentralClearingDTOFilter filter = new CentralClearingDTOFilter();
		filter.addOrderBy(new OrderBy("shortName", ASCENDING));
		return clientServiceManager.findCentralClearingByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardItemStatusDTO> getConfirmStatus(){
		DashBoardItemStatusDTOFilter filter = new DashBoardItemStatusDTOFilter();
		filter.setDashBoardTypeId(Arrays.asList((short)4));
		return gmtClientServiceManager.findDashBoardItemStatusByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardItemTypeDTO> getEventType(){
		return getDashBoardItemTypesForEmatch();
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardItemTypeDTO> getConfirmType(){
		DashBoardItemTypeDTOFilter filter = new DashBoardItemTypeDTOFilter();
		filter.setDashBoardTypeId(Arrays.asList((short)4));
		return clientServiceManager.findDashBoardItemTypeByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardDTO> getConfirmMethod(){
		DashBoardDTOFilter filter = new DashBoardDTOFilter();
		filter.setDashBoardTypeId(Arrays.asList((short)4));
		return clientServiceManager.findDashBoardByFilter(filter);
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardDTO> getElectronicConfirmMethod(){
		DashBoardDTOFilter filter = new DashBoardDTOFilter();
		filter.setDashBoardTypeId(Arrays.asList((short)4));
		filter.setId(Arrays.asList(Short.valueOf("740"), Short.valueOf("741"), Short.valueOf("742"), Short.valueOf("120")));
		return clientServiceManager.findDashBoardByFilter(filter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<CentralClearingDTO> getClearingFacility(){
		CentralClearingDTOFilter filter = new CentralClearingDTOFilter();
		OrderBy orderBy = new OrderBy("id");
		filter.addOrderBy(orderBy);
		return clientServiceManager.findCentralClearingByFilter(filter);
	}
	
	public List<CentralClearingDTO> getClearingFacilityByFilter(CentralClearingDTOFilter filter){
		return clientServiceManager.findCentralClearingByFilter(filter);
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardDTO> getDashBoards(){
        
		DashBoardTypeDTO dbt_dto = getDashBoardTypeDTO();
        DashBoardDTOFilter db_filter = new DashBoardDTOFilter();
        db_filter.setDashBoardTypeId(Arrays.asList(dbt_dto.getDashBoardTypeId()));
        
        return clientServiceManager.findDashBoardByFilter(db_filter);
    }
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardDTO> getDashBoardsForManual(){
        
		DashBoardTypeDTO dbt_dto = getDashBoardTypeDTO();
        DashBoardDTOFilter db_filter = new DashBoardDTOFilter();
        db_filter.setDashBoardTypeId(Arrays.asList(dbt_dto.getDashBoardTypeId()));
        db_filter.setExternalsystemId(Arrays.asList(58));
        return clientServiceManager.findDashBoardByFilter(db_filter);
    }
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public DashBoardTypeDTO getDashBoardTypeDTO(){
		DashBoardTypeDTOFilter dbt_filter = new DashBoardTypeDTOFilter();
        dbt_filter.setShortName(Arrays.asList("TradeExport.Ematch"));
        DashBoardTypeDTO dbt_dto = clientServiceManager.findDashBoardTypeByFilter(dbt_filter).get(0);
        return dbt_dto;
	}

	/**
	 * vshilkar
	 * getDefaultColumnList
	 * List<DefaultColumnsDTO>
	 * @param platformIds
	 * @param profileStatus
	 * @param string 
	 * @return
	 */
	public List<DefaultColumnsDTO> getDefaultColumnList(String platformIds, String profileStatus, String externalSystem) {
		//Map<String, Map<String, List<DefaultColumnsDTO>>> default_column_map = getDefaultColumnMap();		\\ column header population logic is moved in JAVA from DB.
		Map<String, Map<String, List<DefaultColumnsDTO>>> default_column_map = StaticValuesUtil.getDefaultBoardColumnMap();
		List<DefaultColumnsDTO> columnList = null;
		if("0".equals(platformIds)){
			columnList = default_column_map.get(Platform.MANUAL_OTC).get(profileStatus);
		}else{
			columnList = default_column_map.get(externalSystem).get(profileStatus);
		}
		return columnList;
	}

	public List<AttachmentDTO> findDashBoardItemAttachmentByDashBoardItemId(List<BigDecimal> dbiList) {
		return clientServiceManager.findDashBoardItemAttachmentByDashBoardItemId(dbiList);	
	}

	public DashBoardItemDTO findDashBoardItemById(BigDecimal dbItemId) {
		return clientServiceManager.findDashBoardItemById(dbItemId);
	}
	
	public List<DashBoardItemDTO> findDashBoardItemByFilter(DashBoardItemDTOFilter filter) {
		return clientServiceManager.findDashBoardItemByFilter(filter);
	}

	public List getTradeDeliveryAudit(String sql) {
		return gmtClientServiceManager.getTradeDeliveryAudit(sql);
	}

	public List getCommentAudit(String sql){
		return gmtClientServiceManager.getCommentAudit(sql);
	}


	/**
	 * Get User Information from request.
	 * @param request
	 * @return {@link UserSessionDTO}
	 */
	public UserSessionDTO getUserInfo(HttpServletRequest request) {
		UserSessionDTO userSessionDTO = null;
		if(request!=null){
			userSessionDTO = SessionUtil.getUserSessionDTO(request);
			if(userSessionDTO==null){
				userSessionDTO=setUserInfo(request);
			}
		}
		return userSessionDTO;
	}	

	private UserRights getUserRights(HttpServletRequest request) {
		UserRights userRights = getDocumentationEntitlement(request);
		MyEntitlementDTO dto = getMyEntitlement(request);
		userRights.setReadonlyRight(dto.getReadonlyRight());
		userRights.setProcesstradeRight(dto.getProcesstradeRight());
		userRights.setRequestRight(dto.getRequestRight());
		userRights.setAllclientsRight(dto.getAllclientsRight());
		return  userRights;  
	}

	/**
	 * Set the user information into session
	 * @param request
	 * @return {@link UserSessionDTO}
	 */
	private UserSessionDTO setUserInfo(HttpServletRequest request) {
		UserSessionDTO userSessionDTO = new UserSessionDTO();
		try {
			UserDTO userDTO = getUserDTO(request);
			
			userSessionDTO.setUserDTO(userDTO);
			SessionUtil.setUserSessionDTO(request, userSessionDTO);
			userSessionDTO.setUserRoles(getUserRole(request));
			userSessionDTO.setUserRights(getUserRights(request));
			List<Client> authorizedClientList = getAuthorizedClientList(request);
			setClientList(authorizedClientList, userSessionDTO);
			userSessionDTO.setPermitedApps(getPermitedApps(userDTO.getShortName()));
			getTabPermissioning(userSessionDTO);
			userSessionDTO.setInternalUser(isInternalUser(userDTO.getShortName()));
			userSessionDTO.setQuoppaPDFKey(quoppaPDFKey);
			// Set user Type in session
			fetchUserType(userSessionDTO, request);	
			CommonUtil.loggerMessage(log, "UserSessionDTO : ", userSessionDTO.toString());
		} catch (Throwable e) {
			CommonUtil.loggerErrorMessage(log, "setUserInfo", e);
		}
		return userSessionDTO;
	}

	private void fetchUserType(UserSessionDTO userSessionDTO, HttpServletRequest request) {
		if (InterceptorHelper.isSSNCSite(request)) {
			userSessionDTO.setUser(UserType.PORTAL_USER);
		}else{
			if(userSessionDTO.isInternalUser()){
				switch (userSessionDTO.getPermitedApps()) {
				case Database.GOMATCH_SYS:
					userSessionDTO.setUser(UserType.INTERNAL_GOMATCH_USER);
					break;
				case Database.ALL_ACCESS:
				case Database.GOCONFIRM:
					if(userSessionDTO.getUserRights().isOtcUser() 
							&& userSessionDTO.getUserRights().isNonOTCUser()){
						userSessionDTO.setUser(UserType.INTERNAL_USER);
					} else if(userSessionDTO.getUserRights().isOtcUser()){
						userSessionDTO.setUser(UserType.INTERNAL_OTC_USER);
					} else if(userSessionDTO.getUserRights().isNonOTCUser()){
						userSessionDTO.setUser(UserType.INTERNAL_NONOTC_USER);
					}
					break;

				default:
					userSessionDTO.setUser(UserType.INTERNAL_GOMATCH_USER);
					break;
				}
			}else{
				switch (userSessionDTO.getPermitedApps()) {
				case Database.GOMATCH_SYS:
					userSessionDTO.setUser(UserType.EXTERNAL_GOMATCH_USER);
					break;
				case Database.ALL_ACCESS:
				case Database.GOCONFIRM:
					if(userSessionDTO.getUserRights().isOtcUser()){
						userSessionDTO.setUser(UserType.EXTERNAL_OTC_USER);
					}else if(userSessionDTO.getUserRights().isNonOTCUser()){
						userSessionDTO.setUser(UserType.EXTERNAL_NONOTC_USER);
					}else{
						userSessionDTO.setUser(UserType.EXTERNAL_USER);
					}
					break;

				default:
					userSessionDTO.setUser(UserType.EXTERNAL_GOMATCH_USER);
					break;
				}
			}
		}
	}

	public List<Client> getAuthorizedClientList(HttpServletRequest request){
		return entitlementService.getAuthorizedClientList(request, SessionUtil.getUserName(request));
	}
	
	private void setClientList(List<Client> authorizedClientList, UserSessionDTO userSessionDTO) {
		userSessionDTO.setAuthorizedClientFunds(authorizedClientList);
		List<ClientDTO> clients = new ArrayList<>();
		if (CommonUtil.isNullOrEmpty(authorizedClientList) 
				|| userSessionDTO.getUserRights().isAllclientsRight() 		// This is for GoMatch ALL_CLIENT_ACCESS
				|| userSessionDTO.getUserRights().isAllClientAccess()) { 	// This is for GoConfirm ALL_CLIENT_ACCESS
			/*
			 *  If FLP is empty, check if it is an internal user
			 *  For internal users, if all client right is true, get all client list.
			 */
			if(isInternalUser(userSessionDTO.getUserDTO().getShortName())) {
				//if (userSessionDTO.getUserRights().isAllclientsRight()) {
					clients = findClient();		
				//}
			}
		} else {
			List<String> clientNames = new ArrayList<>(authorizedClientList.size());

			List<String> fundNames = new ArrayList<>();
			for (Client client : authorizedClientList) {
				clientNames.add(CommonUtil.getStringValue(client.getClientnamegocheck()));
				fundNames.addAll(client.getFunds().getFundnamegocheck());
			}
			ClientDTOFilter filter = new ClientDTOFilter();
			filter.setShortName(clientNames);
			clients = clientServiceManager.findClientByFilter(filter);

			FundDTOFilter fundFilter = new FundDTOFilter();
			fundFilter.setShortName(fundNames);
			List<FundDTO> funds = filterGoOTCFunds(clientServiceManager.findFundByFilter(fundFilter));
			Map<String, List<FundDTO>> clientFundMap = new HashMap<>(clients.size());	// (ClientId, FundDTO)

			for (FundDTO fund : funds) {
				String clientId = String.valueOf(fund.getClientId());
				if (!clientFundMap.containsKey(clientId)) {
					clientFundMap.put(clientId, new ArrayList<>());
				}
				clientFundMap.get(clientId).add(fund);
			}
			userSessionDTO.setClientFundMap(clientFundMap);
		}
		userSessionDTO.setClientList(clients);
	}
	
	private void getTabPermissioning(UserSessionDTO userSessionDTO) {
		if(userSessionDTO.getUserRights().isPdfApprover() 
				|| userSessionDTO.getUserRights().isPdfReviewer() 
				|| userSessionDTO.getUserRights().isPdfSignature()){
			userSessionDTO.setPaperConfirmTabFlag(true);
		}
		
		if(userSessionDTO.getUserRights().isRunAgingReport()
				|| userSessionDTO.getUserRights().isRunMgmtReport()){
			userSessionDTO.setAgingReportTabFlag(true);
		}
		
		//Added for Fails MANAGEMENT
		if(userSessionDTO.getUserRights().isFailsMgmtProcessing()){
			userSessionDTO.setFailsMgmtTabFlag(true);
		}
	}

	@NonLoggable
	private String getPermitedApps(String userName) {
		boolean isGoMatchAccesss = false;
		boolean isGoConfirmAccesss = false;
		try {
			//List<Object> list = commonSQLExecuterDAO.executeSQL(GET_ENTITLED_APPS_FOR_USER + userName + "'");
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + GET_ENTITLED_APPS_FOR_USER + userName + "'", ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getStringList())){
				for (String object : listDTO.getStringList()) {
					if(Database.GOMATCH.equalsIgnoreCase(object)){
						isGoMatchAccesss = true;
					}
					if(Database.GOCONFIRM.equalsIgnoreCase(object)){
						isGoConfirmAccesss = true;
					}
				}
			}
			if(isGoMatchAccesss && isGoConfirmAccesss){
				return Database.ALL_ACCESS;
			}
			if(isGoMatchAccesss){
				return Database.GOMATCH_SYS;
			}
			if(isGoConfirmAccesss){
				return Database.GOCONFIRM;
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getPermitedApps", e);
		}
		return null;
	}
	
	public boolean isGoconfirmUser(String userName){
		//List<Object> list = commonSQLExecuterDAO.executeSQL(GET_ENTITLED_APPS_FOR_USER + userName + "'");
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + GET_ENTITLED_APPS_FOR_USER + userName + "'", ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && CommonUtil.isNullOrEmpty(listDTO.getStringList()))
		for (String val : listDTO.getStringList()) {
			if(Database.GOCONFIRM.equalsIgnoreCase(val)){
				return true;
			}
		}
		return false;
	}

	private Set<String> getConfUserFund(Integer userId) {
		Set<String> arrConfFunds=ConfUserFundDAO.getInstance().authorizedFundListForUser(userId);
		return arrConfFunds;
	}

	private HashMap<String,Integer> getFundSignatoriesForUserId(Integer userId) {
		HashMap<String,Integer> fundSignatoriesMap=ConfUserFundDAO.getInstance().getFundSignatoriesForUserId(userId);
		return fundSignatoriesMap;
	}

	private HashMap<String,String> getConfUserFundRoleId(Integer userId) {
		HashMap<String,String> ConfUserFundRole=ConfUserFundDAO.getInstance().getConfUserFundRoleId(userId);
		return ConfUserFundRole;
	}

	private HashMap<String,String> getApprovalFundMap(HashMap<String, String> confUserFundRole) {
		HashMap<String,String> UserApproverFund=new HashMap<String,String>();
		if(!CommonUtil.isNullOrEmpty(confUserFundRole)){
			Set<String> keySet = confUserFundRole.keySet();
			Iterator<String> keySetIterator = keySet.iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				if(confUserFundRole.get(key)!=null && confUserFundRole.get(key).equalsIgnoreCase("Document Approver")){
					UserApproverFund.put(key, confUserFundRole.get(key));
				}
			}
		}
		return UserApproverFund;
	}

	/**
	 * Get user role from request
	 * @param request
	 * @return userRoles {@link List}
	 */
	private List<EntitlementUserRoleDTO> getUserRole(HttpServletRequest request) {
		EntitlementUserRoleDTOFilter filter1 = new EntitlementUserRoleDTOFilter();
		filter1.setUserShortName(Arrays.asList(SessionUtil.getUserName(request)));
		List<EntitlementUserRoleDTO> userRoles = clientServiceManager.findEntitlementUserRoleByFilter(filter1);
		return userRoles;
	}

	/**
	 * Get authorized funds from ClientId
	 * @param clientid
	 * @param request
	 * @return {@link List} of {@link FundDTO}
	 * @throws Exception
	 */
	public List<FundDTO> getAuthorizedFunds(Short clientid, HttpServletRequest request) throws Exception {
		List<FundDTO> fundList=null;
		try {
			if(clientid!=null && clientid>0){
				fundList=getFundListFromUserInfo(clientid, request);
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getAuthorizedFunds", e);
		} 
		return fundList;
	}

	/**
	 * Get Fund list form UserInfo
	 * @param clientid
	 * @param request
	 * @return {@link List} of {@link FundDTO}
	 * @throws Exception
	 */
	private List<FundDTO> getFundListFromUserInfo(Short clientid, HttpServletRequest request) throws Exception {
		List<FundDTO> fundList=null;
		try {
			UserSessionDTO userSessionDTO = (UserSessionDTO)getUserInfo(request);
			Map<String, List<FundDTO>> clientFundMap = userSessionDTO.getClientFundMap();

			fundList = clientFundMap.get(CommonUtil.getStringValue(clientid));
			if(CommonUtil.isNullOrEmpty(fundList)){
				List<Short> clients = new ArrayList<Short>();
				clients.add(clientid);
				fundList=getFundByClientId(clients, request);
				if(CommonUtil.isNullOrEmpty(fundList)){
					return fundList;
				}
			}
		} catch (Exception e) {
			throw new Exception("No fund assigned to the userId for the client:."+clientid);
		}
		return fundList;
	}

	public  Set<String> getConfUserFunds(HttpServletRequest request) throws GomatchBaseException {
		Set<String> confUserFund=null;
		try {
			/** check if any fund assigned ti the user of not.. if not 
			 * no need to load the load the PCAA tab data.
			 * and thorw excpetion**/
			Map<String, List<FundDTO>> clientFundMap= SessionUtil.getClientFundMap(request);
			if(clientFundMap==null || clientFundMap.size()==0){
				throw new Exception("No fund assigned to the user.");
			}
			/** set all the sign PCAA data**/
			setSignatureFundsAndRole(request);
			confUserFund=SessionUtil.getAuthorizedFundList(request);
		} 
		catch (Exception e) {
			GomatchExceptionHandler.getInstance().logAndThrow(GomatchErrorDetail.getInstance("02",e.getMessage(),e));
		}
		return confUserFund;
	}	

	public UserSessionDTO setSignatureFundsAndRole(HttpServletRequest request) {
		UserSessionDTO userSessionDTO=getUserInfo(request);
		try {
			UserDTO userDTO=getUserDTO(request);
			userSessionDTO.setConfUserFund(getConfUserFund(userDTO.getId()));
			userSessionDTO.setFundSignatories(getFundSignatoriesForUserId(userDTO.getId()));
			HashMap<String,String> confUserFundRole=getConfUserFundRoleId(userDTO.getId());
			userSessionDTO.setConfUserFundRole(confUserFundRole);
			userSessionDTO.setUserApprovalFundMap(getApprovalFundMap(userSessionDTO.getConfUserFundRole()));
			if(!CommonUtil.isNullOrEmpty(confUserFundRole)){
				userSessionDTO.setConfUserFund(addReviewApproveFund(confUserFundRole,userSessionDTO.getConfUserFund()));
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "setSignatureFundsAndRole", e);
		}
		return userSessionDTO;
	}

	private Set<String> addReviewApproveFund(HashMap<String, String> confUserFundRole, Set<String> confUserFund) {
		try {
			if(confUserFundRole!=null && confUserFundRole.size()>0){
				for (Entry<String, String> entry : confUserFundRole.entrySet()) {
					confUserFund.add(entry.getKey());
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "addReviewApproveFund", e);
		}
		return confUserFund;
	}

	/**
	 * @param entitlementRightDTOList
	 */
	private UserRights setUserRight(List<EntitlementRightDTO> entitlementRightDTOList) {
		UserRights userRights = new UserRights(); 
		for(EntitlementRightDTO entitlementRightDTO : entitlementRightDTOList){
			/*
				if(entitlementRightDTO.getShortName().equalsIgnoreCase("PROCESSTRADES")){
	                myEntitlementDTO.setProcesstradeRight(true);*/

			String setRight = entitlementRightDTO.getShortName();
			switch (setRight) {
			case "Right Addition" : 
				userRights.setRightAddition(true);
				break;

			case "Attach Document" : 
				userRights.setAttachDocument(true);
				break;

			case "DTCC Processing" : 
				userRights.setDtccProcessing(true);
				break;

			case "Delete Confirm" : 
				userRights.setDeleteConfirm(true);
				break;

			case "Delete Document" : 
				userRights.setDeleteDocument(true);
				break;

			case "Non OTC User" : 
				userRights.setNonOTCUser(true);
				break;

			case "OTC User" : 
				userRights.setOtcUser(true);
				break;

			case "Reload Trades" : 
				userRights.setReloadTrades(true);
				break;

			case "Retreive Document" : 
				userRights.setRetreiveDocument(true);
				break;

			case "Run Aging Report" : 
				userRights.setRunAgingReport(true);
				break;

			case "Run Mgmt Report" : 
				userRights.setRunMgmtReport(true);
				break;

			case "Run System Report" : 
				userRights.setRunSystemReport(true);
				break;

			case "Start/Stop Listener" : 
				userRights.setStartStopListener(true);
				break;

			case "Update Confirm" : 
				userRights.setUpdateConfirm(true);
				break;

			case "User Addition" : 
				userRights.setUserAddition(true);
				break;

			case "View Admin Screen" : 
				userRights.setViewAdminScreen(true);
				break;

			//Right for Business Users to access Admin screens
			case "View Admin Screen BU" : 
				userRights.setViewAdminScreenBU(true);
				break;

			case "Create Amendment lines" : 
				userRights.setCreateAmendmentLines(true);
				break;

			case "DTCC Reference Data Admin" : 
				userRights.setDtccReferenceDataAdmin(true);
				break;

			case "PDF Signature" : 
				userRights.setPdfSignature(true);
				break;

			case "DTCC Technical Setup" : 
				userRights.setDtccTechnicalSetup(true);
				break;

			case "Support Activity" : 
				userRights.setSupportActivity(true);
				break;

			case "Self Service" : 
				userRights.setSelfService(true);
				break;

			case "Update Cpty Doc Deal Id" : 
				userRights.setUpdateCptyDocDealId(true);
				break;

			case "DTCC Cancel" : 
				userRights.setDtccCancel(true);
				break;

			case "Update DTCC DealId" : 
				userRights.setUpdateDtccDealId(true);
				break;

			case "Manage GoML" : 
				userRights.setManageGoML(true);
				break;

			case "DTCC Support" : 
				userRights.setDtccSupport(true);
				break;

			//Right to provide read only access to admin screens
			case "Read Only Admin Screen" : 
				userRights.setReadOnlyAdminScreen(true);
				break;

			case "Advanced Support" : 
				userRights.setAdvancedSupportActivity(true);
				break;


			case "Delete EMIR Fields" : 
				userRights.setDeleteEmirFields(true);
				break;


			case "All Client Access" : 
				userRights.setAllClientAccess(true);
				break;


			case "PDF Reviewer" : 
				userRights.setPdfReviewer(true);
				break;


			case "PDF Approver" : 
				userRights.setPdfApprover(true);
				break;

			//Added for Fails MANAGEMENT
			case "Process FM Trades" : 
				userRights.setFailsMgmtProcessing(true);
				break;
				
			default:
				break;
			}
		}
		return userRights;
	}	

	@NonLoggable
	public String convertListToJSON(List<ClientDTO> clientList){
		if (CommonUtil.isNullOrEmpty(clientList)) {
			return null;
		}
		Filter filter = null;
		List<Filter> list = new ArrayList<Filter>();
		for (ClientDTO clientDTO : clientList) {
			filter = new Filter();
			filter.setId(clientDTO.getId().toString());
			filter.setName(clientDTO.getShortName());
			filter.setSELECTED(false);
			list.add(filter);
		}
		return new Gson().toJson(list);
	}

	public List<KeyValueDTO> getCachableFieldList(){
		List<KeyValueDTO> list = new ArrayList<KeyValueDTO>();
		try {
			Field[] fields = RequestMappingConstants.CacheConstants.class.getDeclaredFields();
			for (Field field : fields) {
				KeyValueDTO dto = new KeyValueDTO((String) field.get(null), field.getName());
				list.add(dto);
			}	
		} catch (Throwable e) {
			CommonUtil.loggerErrorMessage(log, "getCachableFieldList", e);
		}
		return list;
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public String getCachableFieldJSON(){		
		List<KeyValueDTO> fieldList = getCachableFieldList();
		Filter filter = null;
		List<Filter> list = new ArrayList<Filter>();
		for (KeyValueDTO dto : fieldList) {
			filter = new Filter();
			filter.setId(dto.getKey());
			filter.setName(dto.getValue());
			filter.setSELECTED(false);
			list.add(filter);
		}
		return new Gson().toJson(list);
	}

	public String goAppsInt(Short clientId){
		return gmtClientServiceManager.goAppsInt(clientId);
	}

	public Map<String, String> getSavedSearchDetails(HttpServletRequest request) {
		Map<String, String> map = null;
		try {
			UserDTO userDTO = SessionUtil.getUserDTO(request);
			String environment = request.getHeader("referer")==null ? request.getRequestURL().toString() : request.getHeader("referer");
			SavedSearchDAO dao = new SavedSearchDAO();
			map = dao.runQuery(userDTO, getSavedSearchPageId(" "), "");			

		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getSavedSearchDetails", e);
		}
		return map;
	}

	public Map<String, String> getSavedSearchDetails(HttpServletRequest request, String pageId) {
		Map<String, String> map = null;
		try {
			UserDTO userDTO = SessionUtil.getUserDTO(request);
			String environment = request.getHeader("referer")==null ? request.getRequestURL().toString() : request.getHeader("referer");
			SavedSearchDAO dao = new SavedSearchDAO();
			map = dao.runQuery(userDTO, getSavedSearchPageId(pageId), environment);			

		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getSavedSearchDetails", e);
		}
		return map;
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, Integer> loadSavedSearchPageId(){
		Map<String, Integer> map = null;
		try{
			if(this.getReferenceDataMap() == null){
				map = new HashMap<String, Integer>();
				List<ReferenceDataDTO> referenceDataList = gmtClientServiceManager.findEmatchReferenceDataByClass(MatchingWeb.SAVEDSEARCHPAGE);
				for (ReferenceDataDTO d : referenceDataList){
					map.put(CommonUtil.trim(d.getShortName()), d.getId());
				}
				this.setReferenceDataMap(map);
			}else{
				map = this.getReferenceDataMap();
			}
		}catch(Exception e){
			CommonUtil.loggerErrorMessage(log, "loadSavedSearchPageId", e);
		}		
		return map;
	}

	@NonLoggable
	public String getSavedSearchPageId(String name){
		Integer id = loadSavedSearchPageId().get(name);
		return id == null ? "" : id.toString(); 
	}

	public BaseDTO savedSearchDetails(SavedSearchDTO searchDTO ) throws GomatchBaseException {
		int id = 0;
		BaseDTO baseDTO = null;
		try {
			SavedSearchDAO dao = new SavedSearchDAO();
			baseDTO = dao.insert(searchDTO);
			searchDTO = (SavedSearchDTO) baseDTO;
			if(searchDTO.isLimitExceedFlag()){
				List<ValidationErrorDTO> messages = new ArrayList<ValidationErrorDTO>();
				messages.add(new ValidationErrorDTO("Saved Search Limit Exceeded.","SAVED_SEARCH_DETAILS - 01"));
				baseDTO.setValidationErrorList(messages);
				baseDTO.setServiceStatus(BaseDTO.BUSINESS_VALIDATION_FAILURE);
			}else{
				baseDTO.setServiceStatus(BaseDTO.SUCCESS);
			}
		} catch (Exception e) {
			GomatchExceptionHandler.getInstance().logAndThrow(GomatchErrorDetail.getInstance("01","GMTService - savedSearchDetails",e));
		}
		return baseDTO;
	}

	public int deleteSavedSearch(SavedSearchDTO searchDTO) {
		int id = 0;
		try {
			SavedSearchDAO dao = new SavedSearchDAO();
			id = dao.delete(searchDTO);

		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "deleteSavedSearch", e);
		}
		return id;
	}

	public List getBreakDetails2(Long dashboardItemId) throws Exception {
        String breakSql =   "select ecf.ShortName as Entity, etb.Source1 as ClientValue, etb.Source2 as CounterpartyValue  " +
                            "from EmatchTradeBreak etb " +
                            "join EmatchComparisonField ecf on (etb.FieldId = ecf.FieldId) " +
                            "where " +
                            "etb.DashBoardItemId = " + dashboardItemId +
                            " and Comments = null";
        return gmtClientServiceManager.getBreakDetails(breakSql);
    }
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<EmatchComparisonFieldDTO> findEmatchComparisonFieldByFilter(EmatchComparisonFieldDTOFilter filter){
		return gmtClientServiceManager.findEmatchComparisonFieldByFilter(filter);
	}
	
	/**
	 * Check if the product isOtcProduct
	 * @param externalSystem
	 * @return boolean
	 */
	public boolean isOtcProduct(String externalSystem) {
		List<ExternalSystemDTO> platforms = getExternalSystem();
		for (ExternalSystemDTO externalSystemDTO : platforms) {
			if (externalSystemDTO.getShortName().equalsIgnoreCase(externalSystem)) {
				return true;
			}
		}
		return false;
	}
	
	public List<com.globeop.go2.client.dto.GoConfirmDashBooardItemTradeDTO> findDBItemTrade(GoConfirmDashBooardItemTradeDTOFilter filter){
		return clientServiceManager.findDBItemTrade(filter).getResults();
	}
	
	/**
	 * getDBIStatusMap 
	 * @param string
	 * @return Map<String,List<Short>>
	 */
	public Map<String, List<Short>> getDBIStatusMap(String className) {
		List<ApplicationNameDTO> goConfirmStatusList = getGoConfirmStatus(className);
		Map<String, List<Short>> goConfirmStatusMap = new HashMap<String, List<Short>>();
		for (ApplicationNameDTO dto : goConfirmStatusList) {
			List<Short> statusList = new ArrayList<Short>();
			String refData = dto.getDisplayName();
			Short dashBoardItemStatusId = dto.getDashBoardTypeId();

			if (goConfirmStatusMap.containsKey(refData)) {
				statusList = goConfirmStatusMap.get(refData);
				statusList.add(dashBoardItemStatusId);
			} else {
				statusList.add(dashBoardItemStatusId);
				goConfirmStatusMap.put(refData, statusList);
			}
		}
		return goConfirmStatusMap;
	}
	
	public List<NoteDTO> getNotes(List<BigDecimal> dbiList) {
		return clientServiceManager.findDashBoardItemNoteByDashBoardItemId(dbiList);
	}
	
	public Object[] getDashBoardItemByCriteria(String captureSystemTradeId, String clientId,String dbItemType) {
		String sql = " select dbi.DashBoardItemId from DashBoardItem dbi " +
			     " join TradeEvent te on (te.TradeEventId=dbi.TradeEventId) join Fund f on (f.FundId=dbi.FundId)" +
				 " join DashBoardItemStatus dbis on (dbi.DashBoardItemStatusId = dbis.DashBoardItemStatusId)" +
			     " join Client c on (c.ClientId=f.ClientId) " +
			     " join DashBoardItemType dbit on (dbit.DashBoardItemTypeId=dbi.DashBoardItemTypeId) " +
			     " where " +
			     " te.CaptureSystemTradeId = '" + captureSystemTradeId + "'" +
			     " and c.ClientId=  " + clientId +
			     " and dbi.DashBoardSessionId in (1,2,12) "+
				 " and dbit.ShortName= '"+dbItemType+"'";
		
		//Object[] results = (Object[]) CommonSQLExecuterDAO.getInstance().executeSQL(sql).get(0);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO)){
			return listDTO.getObjectArrayList().get(0);
		}
		return null;
	}
	
	public boolean checkPreviousDeleted(Long dashBoardItemId){
		boolean flag = false;

		String sql = "select distinct dbis.Description from DashBoardItemAudit dbia, DashBoardItemStatus dbis where dbia.DashBoardItemStatusId = dbis.DashBoardItemStatusId and dbia.DashBoardItemId = " + dashBoardItemId + " and dbis.Description = 'Deleted'";
		String result = gmtClientServiceManager.getDashBoardItemStatusDescription1(sql);
		if(!CommonUtil.isNullOrEmpty(result)){
			flag = true;
		}
		return flag;
	}
	
	public String findCaptureSystemTradeId(BigDecimal dashBoardItemId){
        TradeDTOFilter filter = new TradeDTOFilter();
        filter.setTradeHeaderId(Arrays.asList(clientServiceManager.findDashBoardItemById(dashBoardItemId).getTradeId().longValue()));
        return clientServiceManager.findTradeByFilter(filter).get(0).getCaptureSystemTradeId();
    }
	
	public Map<String, String> checkAffirmed(Long[] dashBoardItemIds, String[] captureSystemTradeIds){
        Map<String, String> affirmedFlag = new HashMap<String, String>();
        for(int i=0; i<dashBoardItemIds.length; i++){
            Long dashBoardItemId = dashBoardItemIds[i];
            String captureSystemTradeId = captureSystemTradeIds[i];

            // check whether trade is affirmed - start
            DashBoardItemDTO dbiDTO = clientServiceManager.findDashBoardItemById(new BigDecimal(dashBoardItemId));
            String dbTypeShortName = CommonUtil.trim(dbiDTO.getDashBoardItemStatusDashBoardTypeShortName());
            String dbiStatusShortName = CommonUtil.trim(dbiDTO.getDashBoardItemStatusShortName());

            if("TradeExport.Ematch".equals(dbTypeShortName) && "AFFIRMED".equals(dbiStatusShortName)){
                affirmedFlag.put(captureSystemTradeId, "The trade " + captureSystemTradeId + " is affirmed.");
            }
            // check whether trade is affirmed - end
        }
        return affirmedFlag;
    }
	
	public String getGFSMailAddress(String userName) {
		return userName + "@sscinc.com";
	}
	
	public List<String> getTransactionTypeShortNames(String actionName, String externalSystem, Long tradeBlockId, String dbiType) {
		List<String> transactionShortNames = null;
		if("MarkitServ".equals(externalSystem)){
            String key = dbiType;
            if("Original".equals(key)){
                if(tradeBlockId==null || tradeBlockId==0){
                    key += "-TRADE";
                }else{
                    key += "-BLOCK";
                }
            }
            transactionShortNames = Arrays.asList(StaticValuesUtil.getTransactionTypeMarkitServ().get(key));
        }else if("Omgeo".equals(externalSystem)){
            if(actionName.equalsIgnoreCase(MatchingWeb.RECALL_ACTION)){
                transactionShortNames = Arrays.asList("Omgeo_Cancel");
            }else{
                String key = dbiType;
                if("Original".equals(key)){
                    if(tradeBlockId==null || tradeBlockId==0){
                        key += "-TRADE";
                    }else{
                        key += "-BLOCK";
                    }
                }
                transactionShortNames = Arrays.asList(StaticValuesUtil.getTransactionTypeOmgeo().get(key));
            }
        }else if("Trax".equals(externalSystem)){
            if(MatchingWeb.RECALL_ACTION.equalsIgnoreCase(actionName)){
                transactionShortNames = Arrays.asList("Trax_Cancel");
            }else{
                String key = dbiType;
                if("Original".equals(key)){
                    if(tradeBlockId==null || tradeBlockId==0){
                        key += "-TRADE";
                    }else{
                        key += "-BLOCK";
                    }
                }
                transactionShortNames = Arrays.asList(StaticValuesUtil.getTransactionTypeTrax().get(key));
            }
        }else if("SWIFT".equals(externalSystem)){
        	if(MatchingWeb.RECALL_ACTION.equalsIgnoreCase(actionName)){
                transactionShortNames = Arrays.asList("SWIFT_Cancel");
            }else{
                String key = dbiType;
                if("Original".equals(key)){
                    if(tradeBlockId==null || tradeBlockId==0){
                        key += "-TRADE";
                    }else{
                        key += "-BLOCK";
                    }
                }
                transactionShortNames = Arrays.asList(StaticValuesUtil.getTransactionTypeSwift().get(key));
            }
        }else if("Traiana".equals(externalSystem)){
        	if(MatchingWeb.RECALL_ACTION.equalsIgnoreCase(actionName)){
                transactionShortNames = Arrays.asList("Traiana_Cancel");
            }else{
                String key = dbiType;
                if("Original".equals(key)){
                    if(tradeBlockId==null || tradeBlockId==0){
                        key += "-TRADE";
                    }else{
                        key += "-BLOCK";
                    }
                }
                transactionShortNames = Arrays.asList(StaticValuesUtil.getTransactionTypeTraiana().get(key));
            }
        }
		return transactionShortNames;
	}

	public List<String> getActivityShortNames(String action, String externalSystem, DashBoardItemDTO dbiDTO) {
		List<String> activityShortNames = null;
		if(action.equalsIgnoreCase(MatchingWeb.RESEND_ACTION)){
			if(externalSystem.equals("MarkitServ")){
				activityShortNames = Arrays.asList("MarkitServ_Modify");
			}else if(externalSystem.equals("Omgeo")){
				activityShortNames = Arrays.asList("Omgeo_" + gmtClientServiceManager.getAdjustedTradeAction(dbiDTO, "REPC", externalSystem));
			}else if(externalSystem.equals("Trax")){
				activityShortNames = Arrays.asList("Trax_NEWM");
			}else if(externalSystem.equals("SWIFT")){
				activityShortNames = Arrays.asList("SWIFT_NEWM");
			}else if(externalSystem.equals("Traiana")){
				activityShortNames = Arrays.asList("Traiana_" + gmtClientServiceManager.getAdjustedTradeAction(dbiDTO, "MODIFY", externalSystem));
			}
		}else if(action.equalsIgnoreCase(MatchingWeb.RECALL_ACTION)){
			if(externalSystem.equals("MarkitServ")){
				activityShortNames = Arrays.asList("MarkitServ_Cancel");
            }else if(externalSystem.equals("Omgeo")){
            	activityShortNames = Arrays.asList("Omgeo_CANC");
            }else if(externalSystem.equals("Trax")){
            	activityShortNames = Arrays.asList("Trax_CANC");
            }else if(externalSystem.equals("SWIFT")){
            	activityShortNames = Arrays.asList("SWIFT_CANC");
            }else if(externalSystem.equals("Traiana")){
            	activityShortNames = Arrays.asList("Traiana_DELETE");
            }
		}else if(action.equalsIgnoreCase(MatchingWeb.MOVE_TO_ELECTRONIC_ACTION)){
			if(externalSystem.equals("MarkitServ")){
                activityShortNames = Arrays.asList("MarkitServ_New");
            }else if(externalSystem.equals("Omgeo")){
                activityShortNames = Arrays.asList("Omgeo_" + gmtClientServiceManager.getAdjustedTradeAction(dbiDTO, "NEWM", externalSystem));
            }else if(externalSystem.equals("Trax")){
                activityShortNames = Arrays.asList("Trax_NEWM");
            }else if(externalSystem.equals("SWIFT")){
                activityShortNames = Arrays.asList("SWIFT_NEWM");
            }else if(externalSystem.equals("Traiana")){
                activityShortNames = Arrays.asList("Traiana_" + gmtClientServiceManager.getAdjustedTradeAction(dbiDTO, "NEW", externalSystem));
            }
		}
		return activityShortNames;
	}
	
	public List<String> getProductTypeShortNames(String externalSystem, String captureSystemTradeId) {
		List<String> productTypeShortNames = null;
		if(externalSystem.equals("MarkitServ")){
			productTypeShortNames = Arrays.asList("MarkitServ_InterestSwap");
        }else if(externalSystem.equals("Omgeo")){
            String tradeTypeCode = captureSystemTradeId.substring(0, 3);
            productTypeShortNames = Arrays.asList("Omgeo_" + StaticValuesUtil.getProductTypeTranslation().get(tradeTypeCode));
        }else if(externalSystem.equals("Trax")){
        	productTypeShortNames = Arrays.asList("Trax_REPO");
        }else if(externalSystem.equals("SWIFT")){
        	productTypeShortNames = Arrays.asList("SWIFT_FXFORWARD");
        }else if(externalSystem.equals("Traiana")){
            String tradeTypeCode = captureSystemTradeId.substring(0, 3);
            productTypeShortNames = Arrays.asList("Traiana_" + StaticValuesUtil.getProductTypeTranslation().get(tradeTypeCode));
        }
		return productTypeShortNames;
	}

	public List<ReferenceDataDTO> getReferenceDataForExportRequest(String className, List<String> shortNames){
		if(shortNames==null){return null;}
		ReferenceDataDTOFilter filter = new ReferenceDataDTOFilter();
		filter.setClassName(Arrays.asList(className));
		filter.setShortName(shortNames);
		return clientServiceManager.findReferenceDataByFilter(filter);
	}
	
	public EmatchExportRequestDTO getEmatchExportRequestById(Long requestId){
        return gmtClientServiceManager.findEmatchExportRequestById(requestId);
    }

    public List<EmatchExportRequestDTO> getEmatchExportRequestByFilter(EmatchExportRequestDTOFilter filter){
        return gmtClientServiceManager.findEmatchExportRequestByFilter(filter);
    }
    
    public EmatchExportRequestDTO persistEmatchExportRequest(EmatchExportRequestDTO dto){
        return gmtClientServiceManager.persistEmatchExportRequest(dto);
    }
    
    public Map<String, String> sendEmatchTrades(List<Long> dashBoardItemIds, Integer userId) {
		return gmtClientServiceManager.sendEmatchTrades(dashBoardItemIds, userId, null);
	}
    
    public DashBoardItemDTO getDashBoardItemById(BigDecimal dashboardItemId){
        return clientServiceManager.findDashBoardItemById(dashboardItemId);
    }
    
    public EmatchDashBoardItemDTO getEmatchDashBoardItemById(Long dashboardItemId){
        return gmtClientServiceManager.findEmatchDashBoardItemById(dashboardItemId);
    }
    
    public List<EmatchDashBoardItemDTO> getEmatchDashBoardItemByFilter(EmatchDashBoardItemDTOFilter filter){
        return gmtClientServiceManager.findEmatchDashBoardItemByFilter(filter);
    }
    
    public EmatchTradeStatusRequestDTO getEmatchTradeStatusRequestById(Integer id){
        return gmtClientServiceManager.findEmatchTradeStatusRequestById(id);
    }

    public List<EmatchTradeStatusRequestDTO> getEmatchTradeStatusRequestByFilter(EmatchTradeStatusRequestDTOFilter filter){
        return gmtClientServiceManager.findEmatchTradeStatusRequestByFilter(filter);
    }

    public EmatchTradeStatusRequestDTO insertEmatchTradeStatusRequest(EmatchTradeStatusRequestDTO dto){
        return gmtClientServiceManager.persistEmatchTradeStatusRequest(dto);
    }
    
    public List<TradeDTO> findTradeByFilter(TradeDTOFilter filter) {
    	return clientServiceManager.findTradeByFilter(filter);
	}
    
    public FundDTO findFundById(Integer integer){
        return clientServiceManager.findFundById(integer);
    }
    
    public ClientDTO findClientById(Short id){
        return clientServiceManager.findClientById(id);
    }
    
    public Long getTradeBlockId(Integer id){
        try {
			return gmtClientServiceManager.getTradeBlockId(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    public String getTradeStatusRequestType(String actionName) {
    	if(actionName.equals(MatchingWeb.FORCE_DELETE_ACTION)){
    		return "D";
    	} else if(actionName.equals(MatchingWeb.MOVE_TO_MANUAL_ACTION)){
    		return "M";
    	} else{
    		return "C";
    	}
    }
    
    public EmatchDashBoardStatusMapDTO findEmatchEmatchDashBoardStatusMapById(Integer requestId) {
		return gmtClientServiceManager.findEmatchDashBoardStatusMapById(requestId);
	}
	
	public List<EmatchDashBoardStatusMapDTO> findEmatchDashBoardStatusMapByFilter(EmatchDashBoardStatusMapDTOFilter filter) {
		return gmtClientServiceManager.findEmatchDashBoardStatusMapByFilter(filter);
	}
	
	public EmatchDashBoardItemDTO insertEmatchDashBoardItem(EmatchDashBoardItemDTO ematchDashBoardItemDTO){
        return gmtClientServiceManager.persistEmatchDashBoardItem(ematchDashBoardItemDTO);
    }
	
	public void manualRequestPendingAction(List<Long> dashBoardItemIds, UserDTO user, String dashBoardItemComment){

		Timestamp current_time = new Timestamp(new Date().getTime());

		// fetch manual dashboards, external system, external system status - start
		Integer externalSystemId = null;
		List<DashBoardDTO> manual_dashboards = getDashBoards();
		Short nonOtc_dbId = null, otc_dbId = null;
		for(DashBoardDTO dto : manual_dashboards)
		{
			String dbShortName = dto.getShortName().trim();
			if(dbShortName.equals("MANUAL_PAPER_NONOTC"))
			{
				nonOtc_dbId = dto.getId();
				externalSystemId = dto.getExternalsystemId();
			}else if(dbShortName.equals("MANUAL_PAPER_OTC")){
				otc_dbId = dto.getId();
				externalSystemId = dto.getExternalsystemId();
			}
		}

		Short statusId = null;
		List<DashBoardItemStatusDTO> status_list = getDashBoardItemStatus();
		for(DashBoardItemStatusDTO dto : status_list){if(dto.getShortName().equals("NEW")){statusId = dto.getId();}}
		// fetch manual dashboards, external system, external system status - end

		for(Long dashBoardItemId : dashBoardItemIds)
		{
			DashBoardItemDTO dbi = getDashBoardItemById(new BigDecimal(dashBoardItemId));
			EmatchDashBoardItemDTO edbi = getEmatchDashBoardItemById(dashBoardItemId);

			Boolean otcFlag = (getTradeTypeByDashBoardIds(Arrays.asList(dbi.getDashBoardId()))).get(0).getOtcFlag();
			Short dbId = nonOtc_dbId;
			if(otcFlag){dbId = otc_dbId;}

			// update dashboard item - start
			dbi.setDashBoardId(dbId);
			dbi.setExternalSystemId(externalSystemId);
			dbi.setDashBoardItemStatusId(statusId);
			dbi.setLastStatusChangeDateTime(current_time);
			dbi.setLastModifiedUser(user);
			dbi.setLastModifiedDateTime(null);
			persistDashBoardItem(dbi);
			// update dashboard item - end

			// update ematch dashboard item - start
			edbi.setComments(dashBoardItemComment);
			edbi.setLastModifiedUserId(user.getId());
			edbi.setLastModifiedDateTime(null);
			insertEmatchDashBoardItem(edbi);
			// update ematch dashboard item - end
		}
	}
    
    /**
	 * Checks if the two values are equal
	 * <li> If both values are null, return true.</li>
	 * <li> If either value is null, return false.</li>
	 * <li> If both values are of different class, return false.</li>
	 * <li> If both values are of type String, compare after trimming.</li>
	 * <li> Compare if values are equal.<p>
	 * @param value1
	 * @param value2
	 * @return
	 */
	public boolean isEqual(Object value1, Object value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null && value2 != null) {
			return false;
		}
		if (value1 != null && value2 == null) {
			return false;
		}
		
		if (value1.getClass().equals(value2.getClass())) {
			if ((value1 instanceof String) && (value2 instanceof String)) {
				return String.valueOf(value1).trim().equals(String.valueOf(value2).trim());	
			} else {
				return value1.equals(value2);
			}	
		} else {
			return false;
		}
	}
	
	public String validateDashboardItemId(Long dashBoardItemId, String captureSystemTradeId) {
		EmatchTradeStatusRequestDTOFilter filter = new EmatchTradeStatusRequestDTOFilter();
		List<BigDecimal> dbItemId = new ArrayList<BigDecimal>();
		dbItemId.add(new BigDecimal(dashBoardItemId));
		filter.setDashBoardItemId(dbItemId);
		List<EmatchRequestStatusDTO> status = new ArrayList<EmatchRequestStatusDTO>();
		status.add(EmatchRequestStatusDTO.PENDING);
		filter.setStatus(status);
		List<EmatchTradeStatusRequestDTO> tradeStatusRequestDTOs = getEmatchTradeStatusRequestByFilter(filter);
		
		DashBoardItemDTO dashBoardItemDTO = getDashBoardItemById(new BigDecimal(dashBoardItemId));
		
		if(dashBoardItemDTO != null && !dashBoardItemDTO.getExternalSystemShortName().equalsIgnoreCase("Manual")){
			return "Trade " + captureSystemTradeId + " is invalid for Move to Electronic.";
		} else{
			if(tradeStatusRequestDTOs != null && tradeStatusRequestDTOs.size()>0){
				return "Request for Trade " + captureSystemTradeId + " is already in process.";
			}
		}
		return null;
	}
	
	public String fetchClientProductMappingForDBItemId(Long dashBoardItemId) {
		String returnString = "";
		StoredProcResults spResults;
		try {
			List<String> inputParams = new ArrayList<String>();
			//inputParams.add("60495793");
			inputParams.add(dashBoardItemId.toString());
			inputParams.add(null);

			//spResults = executeStoredProc("EmatchEligibleTrade", inputParams.toArray(), new String[]{"VARCHAR", "TINYINT"});
			spResults = executeStoredProcReadOnly("EmatchEligibleTrade", inputParams.toArray(), new String[]{"VARCHAR", "TINYINT"});
			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			
			for(StoredProcResultSet spResultSet : spResultSets)
			{
				BigDecimal dbItemId = ((BigDecimal)spResultSet.getObject(1, 1));
				Integer clientId = ((Integer)spResultSet.getObject(1, 2));
				String captureSystemTradeId = ((String)spResultSet.getObject(1, 3));
				Integer dashBoardId = ((Integer)spResultSet.getObject(1, 4));
				Integer externalSystemId = ((Integer)spResultSet.getObject(1, 5));
				String externalSystem = ((String)spResultSet.getObject(1, 6));
				
				if(externalSystem == null || externalSystem == "" || externalSystem.equalsIgnoreCase("Manual")){
					returnString = captureSystemTradeId + " is invalid for Move to Electronic process.";
				}else{
					returnString = externalSystemId.toString() + "-" + externalSystem + "-" + dashBoardId + "-" + captureSystemTradeId + "-" + dbItemId;
				}
			}
		} catch (SQLException e) {
			log.info(" ======= Excpetion while fetchClientProductMappingForDBItemId ======= ",e);
			e.printStackTrace();
		} catch (Exception e) {
			log.info(" ======= Excpetion while fetchClientProductMappingForDBItemId ======= ",e);
			e.printStackTrace();
		} 
		return returnString;
	}
	
	public void completeRequestPendingAction(List<BigDecimal> dashBoardItemIds, UserDTO userDTO, String dashBoardItemComment){
		
		try {
			DashBoardTypeDTO dashBoardTypeDTO = getDashBoardTypeDTO();
			Timestamp current_time = new Timestamp(new Date().getTime());
			
			for(BigDecimal dashBoardItemId : dashBoardItemIds){
				// find ematch dashboard item - start
				EmatchDashBoardItemDTO edbi_dto = getEmatchDashBoardItemById(dashBoardItemId.longValue());
				// find ematch dashboard item - end

				// find dashboard item - start
				DashBoardItemDTO dbi_dto = findDashBoardItemById(dashBoardItemId);
				// find dashboard item - end

				// find central clearing - start
				Integer centralClearingId = getCentralClearingId(dbi_dto.getTradeId());
				CentralClearingDTOFilter cc_filter = new CentralClearingDTOFilter();
				cc_filter.setShortName(Arrays.asList("NONE", "NOT CLEARED"));
				List<CentralClearingDTO> cc_dto_list = getClearingFacilityByFilter(cc_filter);
				List<Integer> cc_list = new ArrayList<Integer>();
				for(CentralClearingDTO dto : cc_dto_list){
					cc_list.add(dto.getId());
				}
				// find central clearing - end

				// find external system - start
				String es_str = getExternalSystemById(dbi_dto.getExternalSystemId()).getShortName();
				// find external system - end

				// find block status - start
				EmatchBlockStatusDTO ebs_dto = null;
				EmatchBlockStatusDTOFilter ebs_filter = new EmatchBlockStatusDTOFilter();
				ebs_filter.setExternalSystemShortName(Arrays.asList(es_str));
				if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_OMGEO)){
					ebs_filter.setShortName(Arrays.asList("MACH"));
				}else if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_MARKIT)){
					ebs_filter.setShortName(Arrays.asList("Confirmed"));
				}
				List<EmatchBlockStatusDTO> ebs_dto_list = getEmatchBlockStatusByFilter(ebs_filter);
				if(ebs_dto_list!=null && !ebs_dto_list.isEmpty()){
					ebs_dto = ebs_dto_list.get(0);
				}
				// find block status - end

				// find clearing status - start
				EmatchClearingStatusDTO ecs_dto = null;
				EmatchClearingStatusDTOFilter ecs_filter = new EmatchClearingStatusDTOFilter();
				ecs_filter.setExternalSystemShortName(Arrays.asList(es_str));
				if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_OMGEO)){
					ecs_filter.setShortName(Arrays.asList("MAGR"));
				}else if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_MARKIT)){
					ecs_filter.setShortName(Arrays.asList("Cleared"));
				}
				List<EmatchClearingStatusDTO> ecs_dto_list = getEmatchClearingStatusByFilter(ecs_filter);
				if(ecs_dto_list!=null && !ecs_dto_list.isEmpty())
				{
					ecs_dto = ecs_dto_list.get(0);
				}
				// find clearing status - end

				// find external system status - start
				EmatchExternalSystemStatusDTO eess_dto = null;
				EmatchExternalSystemStatusDTOFilter eess_filter = new EmatchExternalSystemStatusDTOFilter();
				eess_filter.setExternalSystemShortName(Arrays.asList(es_str));
				if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_OMGEO)){
					eess_filter.setShortName(Arrays.asList("MACH"));
				}else if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_MARKIT)){
					eess_filter.setShortName(Arrays.asList("Confirmed"));
				}else if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_TRAX)){
					eess_filter.setShortName(Arrays.asList("MMS"));
				}else if(es_str.equalsIgnoreCase(Platform.EXTERNALSYSTEM_SWIFT)){
					eess_filter.setShortName(Arrays.asList("Confirmed"));
				}
				eess_dto = getEmatchExternalSystemStatusByFilter(eess_filter).get(0);
				// find external system status - end

				// find dashboard item status - start
				DashBoardItemStatusDTOFilter dbis_filter = new DashBoardItemStatusDTOFilter();
				dbis_filter.setDashBoardTypeId(Arrays.asList(dashBoardTypeDTO.getDashBoardTypeId()));
				dbis_filter.setShortName(Arrays.asList("FORCE-AFFIRMED"));
				DashBoardItemStatusDTO dbis_dto = getDashBoardItemStatusByFilter(dbis_filter).get(0);
				// find dashboard item status - end

				// update ematch dashboard item - start
				edbi_dto.setComments(dashBoardItemComment);
				if(ebs_dto!=null)
				{
					edbi_dto.setBlockStatusId(ebs_dto.getBlockStatusId());
					edbi_dto.setBlockStatusChangeDateTime(current_time);
				}
				if(centralClearingId!=null && !cc_list.contains(centralClearingId))
				{
					edbi_dto.setClearingStatusId(ecs_dto.getClearingStatusId());
					edbi_dto.setClearingStatusChangeDateTime(current_time);
				}
				edbi_dto.setExternalSystemStatusId(eess_dto.getExternalSystemStatusId());
				edbi_dto.setLastExtStatChangeDateTime(current_time);
				edbi_dto.setLastModifiedUserId(userDTO.getId());
				edbi_dto.setLastModifiedDateTime(null);
				gmtClientServiceManager.persistEmatchDashBoardItem(edbi_dto);
				// update ematch dashboard item - end

				// update dashboard item - start
				dbi_dto.setDashBoardItemStatusId(dbis_dto.getId());
				dbi_dto.setSeverity(SeverityDTO.COMPLETED);
				dbi_dto.setLastStatusChangeDateTime(current_time);
				dbi_dto.setLastModifiedUser(userDTO);
				dbi_dto.setLastModifiedDateTime(null);
				persistDashBoardItem(dbi_dto);
				// update dashboard item - end
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	
	public void saveAttachmentByDashBoardItemId(List<BigDecimal> dashBoardItemIds, String actionName) throws GomatchBaseException {
		try {
			// attachment logic - start
			for(BigDecimal dashBoardItemId : dashBoardItemIds){
				DashBoardItemDTO dashBoardItem = getDashBoardItemById(dashBoardItemId);
				List<AttachmentDTO> attachments = dashBoardItem.getAttachments();
				List<AttachmentDTO> temp_attachments = new ArrayList<AttachmentDTO>();

				// find maximum attachment number - start
				Integer maxAttachmentNumber = -1;
				for(AttachmentDTO attachment : attachments){
					if(attachment.getAttachmentNumber()>maxAttachmentNumber){
						maxAttachmentNumber = attachment.getAttachmentNumber();
					}
				}
				Integer count = maxAttachmentNumber + 1;
				// find maximum attachment number - end

				for(AttachmentDTO dto : attachments){
					if(dto.getName().startsWith("R_")){
						String fileName = dto.getName();
						String filePath = dto.getDocumentPath();

						if(!actionName.equalsIgnoreCase("decline")){
							// rename file - start
							File file = new File(filePath + fileName);
							File newFile = new File(filePath + fileName.substring(2));
							file.renameTo(newFile);
							// rename file - end

							// create attachment - start
							AttachmentDTO attachmentDTO = new AttachmentDTO();
							attachmentDTO.setDashBoardItemId(dto.getDashBoardItemId());
							attachmentDTO.setDocumentTypeId(dto.getDocumentTypeId());
							attachmentDTO.setAttachmentNumber(count);
							attachmentDTO.setName(fileName.substring(2));
							attachmentDTO.setDocumentPath(dto.getDocumentPath());
							attachmentDTO.setLastModifiedUser(dto.getLastModifiedUser());
							attachmentDTO.setLastModifiedDateTime(new Timestamp(new Date().getTime()));
							// create attachment - end

							temp_attachments.add(attachmentDTO);
							count++;
						} else {
							// delete file - start
                            File file = new File(filePath + fileName);
                            file.delete();
                            // delete file - end
						}
					}else{
						// create attachment - start
						AttachmentDTO attachmentDTO = new AttachmentDTO();
						attachmentDTO.setDashBoardItemId(dto.getDashBoardItemId());
						attachmentDTO.setDocumentTypeId(dto.getDocumentTypeId());
						attachmentDTO.setAttachmentNumber(count);
						attachmentDTO.setName(dto.getName());
						attachmentDTO.setDocumentPath(dto.getDocumentPath());
						attachmentDTO.setLastModifiedUser(dto.getLastModifiedUser());
						attachmentDTO.setLastModifiedDateTime(new Timestamp(new Date().getTime()));
						// create attachment - end

						temp_attachments.add(attachmentDTO);
						count++;
					}
				}

				// persist empty attachment list - start
				dashBoardItem.setAttachments(new ArrayList<AttachmentDTO>());
				dashBoardItem = persistDashBoardItem(dashBoardItem);
				// persist empty attachment list - end

				// persist new attachment list - start
				dashBoardItem = getDashBoardItemById(dashBoardItem.getId());
				dashBoardItem.setAttachments(temp_attachments);
				persistDashBoardItem(dashBoardItem);
				// persist new attachment list - end
			}
			// attachment logic - end
		} catch (Exception e) {
			GomatchExceptionHandler.getInstance().logAndThrow(GomatchErrorDetail.getInstance("01", "Exception while saving attachment ", e));
		}
	}
	
	public void deleteRequestPendingAction(List<BigDecimal> dashBoardItemIds, UserDTO user,String dashBoardItemComment) throws GomatchBaseException {
		
		try {
			DashBoardTypeDTO dbt_dto = getDashBoardTypeDTO();
			Timestamp current_time = new Timestamp(new Date().getTime());
			
			 for(BigDecimal dashBoardItemId : dashBoardItemIds){
	             // find ematch dashboard item - start
	             EmatchDashBoardItemDTO edbi_dto = getEmatchDashBoardItemById(dashBoardItemId.longValue());
	             // find ematch dashboard item - end

	             // find dashboard item - start
	             DashBoardItemDTO dbi_dto = clientServiceManager.findDashBoardItemById(dashBoardItemId);
	             // find dashboard item - end

	             // find status to update to - start
	             String status = dbi_dto.getDashBoardItemStatusShortName().trim();
	                 if(status.equals("CONFIRMED") || status.equals("AFFIRMED")){
	                     status = "MATCH-DELETED";
	                 }else{
	                     status = "DELETED";
	                 }
	             // find status to update to - end

	             // find dashboard item status - start
	             DashBoardItemStatusDTOFilter dbis_filter = new DashBoardItemStatusDTOFilter();
	                 dbis_filter.setDashBoardTypeId(Arrays.asList(dbt_dto.getDashBoardTypeId()));
	                 dbis_filter.setShortName(Arrays.asList(status));
	             DashBoardItemStatusDTO dbis_dto = getDashBoardItemStatusByFilter(dbis_filter).get(0);
	             // find dashboard item status - end

	             // update ematch dashboard item - start
	             edbi_dto.setComments(dashBoardItemComment);
	             edbi_dto.setLastModifiedUserId(user.getId());
	             edbi_dto.setLastModifiedDateTime(null);
	             gmtClientServiceManager.persistEmatchDashBoardItem(edbi_dto);
	             // update ematch dashboard item - end

	             // update dashboard item - start
	             dbi_dto.setDashBoardItemStatusId(dbis_dto.getId());
	             dbi_dto.setSeverity(SeverityDTO.COMPLETED);
	             dbi_dto.setLastStatusChangeDateTime(current_time);
	             dbi_dto.setLastModifiedUser(user);
	             dbi_dto.setLastModifiedDateTime(null);
	             persistDashBoardItem(dbi_dto);
	             // update dashboard item - end
	         }
			
		} catch (Exception e) {
			GomatchExceptionHandler.getInstance().logAndThrow(GomatchErrorDetail.getInstance("01", "Exception while force delete ", e));
		}
	}
	
	public String fetchClientProductMappingForDBItemId(String dashBoardItemId) {
		String returnString = "";
		StoredProcResults spResults;
		try {
			List<String> inputParams = new ArrayList<String>();
			//inputParams.add("60495793");
			inputParams.add(dashBoardItemId);
			inputParams.add(null);

			//spResults = executeStoredProc("EmatchEligibleTrade", inputParams.toArray(), new String[]{"VARCHAR", "TINYINT"});
			spResults = executeStoredProcReadOnly("EmatchEligibleTrade", inputParams.toArray(), new String[]{"VARCHAR", "TINYINT"});
			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			
			for(StoredProcResultSet spResultSet : spResultSets)
			{
				BigDecimal dbItemId = ((BigDecimal)spResultSet.getObject(1, 1));
				Integer clientId = ((Integer)spResultSet.getObject(1, 2));
				String captureSystemTradeId = ((String)spResultSet.getObject(1, 3));
				Integer dashBoardId = ((Integer)spResultSet.getObject(1, 4));
				Integer externalSystemId = ((Integer)spResultSet.getObject(1, 5));
				String externalSystem = ((String)spResultSet.getObject(1, 6));
				
				if(externalSystem == null || externalSystem == "" || externalSystem.equalsIgnoreCase("Manual")){
					returnString = captureSystemTradeId + " is invalid for Move to Electronic process.";
				}else{
					returnString = externalSystemId.toString() + "-" + externalSystem + "-" + dashBoardId + "-" + captureSystemTradeId + "-" + dbItemId;
				}
			}
		} catch (SQLException e) {
			log.info(" ======= Excpetion while fetchClientProductMappingForDBItemId ======= ",e);
			e.printStackTrace();
		} catch (Exception e) {
			log.info(" ======= Excpetion while fetchClientProductMappingForDBItemId ======= ",e);
			e.printStackTrace();
		} 
		return returnString;
	}
	
	public Integer getCentralClearingId(BigDecimal tradeId){
		String sql =    "select th.CentralClearingId from TradeHeader th where th.TradeId = " + tradeId;
        return gmtClientServiceManager.getCentralClearingId1(sql);
	}
	
	public List<EmatchBlockStatusDTO> getEmatchBlockStatusByFilter(EmatchBlockStatusDTOFilter filter){
		return gmtClientServiceManager.findEmatchBlockStatusByFilter(filter);
	}
	
	public List<EmatchClearingStatusDTO> getEmatchClearingStatusByFilter(EmatchClearingStatusDTOFilter filter){
		return gmtClientServiceManager.findEmatchClearingStatusByFilter(filter);
	}
	
	public List<EmatchExternalSystemStatusDTO> getEmatchExternalSystemStatusByFilter(EmatchExternalSystemStatusDTOFilter filter){
		return gmtClientServiceManager.findEmatchExternalSystemStatusByFilter(filter);
	}
	
	public List<DashBoardItemStatusDTO> getDashBoardItemStatusByFilter(DashBoardItemStatusDTOFilter filter){
		return gmtClientServiceManager.findDashBoardItemStatusByFilter(filter);
	}
	
	public List<EmatchFileDeleteRequestDTO> getEmatchFileDeleteRequest(EmatchFileDeleteRequestDTOFilter filter){
        return gmtClientServiceManager.findEmatchFileDeleteRequestByFilter(filter);
    }
	
	public EmatchFileDeleteRequestDTO insertEmatchFileDeleteRequest(EmatchFileDeleteRequestDTO dto){
        return gmtClientServiceManager.persistEmatchFileDeleteRequest(dto);
    }
	
	public EmatchExportRequestDTO insertEmatchExportRequest(EmatchExportRequestDTO dto){
        return gmtClientServiceManager.persistEmatchExportRequest(dto);
    }
	
	public void recallRequestPendingAction(Long dashBoardItemId, Integer requestUserId){
        gmtClientServiceManager.recallRequestPendingAction1(Arrays.asList(dashBoardItemId), requestUserId);
    }
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<DashBoardItemTypeDTO> getDashBoardItemTypesForEmatch(){
		DashBoardItemTypeDTOFilter dbitDTOFilter = new DashBoardItemTypeDTOFilter();
        dbitDTOFilter.setDashBoardTypeShortName(Arrays.asList("TradeExport.Ematch"));
        return clientServiceManager.findDashBoardItemTypeByFilter(dbitDTOFilter);
	}

	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String,List> getEmatchDashBoardStatusMapByFilter(){
		
		Map<String,List> confirmMap = new HashMap<String,List>();
		EmatchDashBoardStatusMapDTOFilter edbsmdf = new EmatchDashBoardStatusMapDTOFilter();
		edbsmdf.setDashBoardShortName(Arrays.asList("MANUAL_PAPER_OTC","MANUAL_PAPER_NONOTC"));
		List<EmatchDashBoardStatusMapDTO> ematchDBStatusMapDTOList = gmtClientServiceManager.findEmatchDashBoardStatusMapByFilter(edbsmdf);
		List otcList = new ArrayList();
		List nonOtcList = new ArrayList();
		for(int i=0;i<ematchDBStatusMapDTOList.size();i++){
			EmatchDashBoardStatusMapDTO edbDto = ematchDBStatusMapDTOList.get(i);
			String dbShortName = edbDto.getDashBoardShortName();
			String dashBoardItemStatusId = String.valueOf(edbDto.getDashBoardItemStatusId());
			if(dbShortName.equalsIgnoreCase("MANUAL_PAPER_OTC")){
				otcList.add(dashBoardItemStatusId);
				confirmMap.put(dbShortName, otcList);
			}else if(dbShortName.equalsIgnoreCase("MANUAL_PAPER_NONOTC")){
				nonOtcList.add(dashBoardItemStatusId);
				confirmMap.put(dbShortName, nonOtcList);
			}
		}
		return confirmMap;
	}
	
	public EmatchFileUploadRequestDTO persistBulkUploadDetails(EmatchFileUploadRequestDTO uploadRequestDTO) {
		return gmtClientServiceManager.persistEmatchFileUploadRequest(uploadRequestDTO);
	}
	
	public List<EmatchFileUploadRequestDTO> getEmatchFileUploadRequestDetails(Integer userId){
    	EmatchFileUploadRequestDTOFilter uploadRequestDTOFilter = new EmatchFileUploadRequestDTOFilter();
	    	List<Integer> userIds = new ArrayList<Integer>();
	    	userIds.add(userId);
	    	uploadRequestDTOFilter.setRequestedByUserId(userIds);
		return gmtClientServiceManager.findEmatchFileUploadRequestByFilter(uploadRequestDTOFilter);
    }
	
	public List<EmatchFileUploadRequestDTO> findEmatchFileUploadRequestById(BigDecimal requestId) {
		EmatchFileUploadRequestDTOFilter uploadRequestDTOFilter = new EmatchFileUploadRequestDTOFilter();
	    	List<BigDecimal> ids = new ArrayList<BigDecimal>();
	    	ids.add(requestId);
	    	uploadRequestDTOFilter.setId(ids);
		return gmtClientServiceManager.findEmatchFileUploadRequestByFilter(uploadRequestDTOFilter);
	}
	
	public String processAllegedData(ManagerialButtonFormBean formBean){
			
			String resultStr 	 = "";
			EmatchTradeBlockDTOFilter dtoFilter   = new EmatchTradeBlockDTOFilter();
			int tradeCount  	  		          = formBean.getCaptureSystemIds().length;
			List<String> externalSystemTradeId 	  = new ArrayList<String>();
			
			for(int i = 0; i < tradeCount; i++){
				externalSystemTradeId.add(formBean.getCaptureSystemIds()[i]);
			}
			dtoFilter.setExternalSystemId(Arrays.asList(formBean.getExternalSystemId()));
			dtoFilter.setExternalSystemTradeId(externalSystemTradeId);
			List<EmatchTradeBlockDTO> ematchTradeBlockList = gmtClientServiceManager.findEmatchTradeBlockByFilter(dtoFilter);
			
			if(!CommonUtil.isNullOrEmpty(ematchTradeBlockList)){
				for(EmatchTradeBlockDTO ematchTradeBlock : ematchTradeBlockList){
					if(ematchTradeBlock.getBlockStatusId() != 33){
						EmatchTradeBlockDTO ematchTrdBlc = ematchTradeBlock;
						ematchTrdBlc.setBlockStatusId(Short.valueOf("33"));
						EmatchTradeBlockDTO matchTradeBlockDTOdb = gmtClientServiceManager.persistEmatchTradeBlock(ematchTrdBlc);
						if(matchTradeBlockDTOdb != null){
							resultStr = "Prossed";
						}
					}
				}
			}
			
			List<String> tradeEventIds = findAllocationAllegedData(externalSystemTradeId,formBean);
			String tradeEventids       = tradeEventIds==null?null:String.join(",", tradeEventIds);
			if(!CommonUtil.isNullOrEmpty(tradeEventIds)){
				StringBuilder sb = new StringBuilder();
				sb.append(" update EmatchTradeEvent set AllocationStatusId = 19 where ");
				sb.append(" TradeEventId in ("+tradeEventids+") ");
				//int  i = CommonSQLExecuterDAO.getInstance().updateSQL(sb.toString());
				int i = restTemplate.getForObject(matchingServiceURL + "/executeUpdateSQL.go?query=" + sb.toString(), Integer.class);
				if(i>0){
					     resultStr += "Prossed";
				}
			}
			if(!CommonUtil.isNullOrEmpty(resultStr)){
				resultStr = "Original Trade Id : "+externalSystemTradeId+" Processed Successfully"+"\n";
			}
			return resultStr;
		}
		
	public List<String> findAllocationAllegedData(List<String> externalSystemTradeId,ManagerialButtonFormBean formBean){
		List<String> list =new ArrayList();
		try {
			String ids = "";
			for(int i=0;i<externalSystemTradeId.size();i++){
				if(ids.equals("")){
					ids = "'"+externalSystemTradeId.get(i)+"'";
				}else{
					ids += "'"+externalSystemTradeId.get(i)+"'";
				}
				if(i != externalSystemTradeId.size()-1){
					ids += ",";
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("    select      ");
			sb.append("    te.TradeEventId      ");
			sb.append("    from EmatchTradeBlock tb      ");
			sb.append("    join EmatchBlockStatus ebs on(tb.BlockStatusId = ebs.BlockStatusId)      ");
			sb.append("    join EmatchTradeHeader th on(tb.TradeBlockId = th.TradeBlockId)      ");
			sb.append("    join EmatchTradeEvent te on(th.TradeId = te.TradeId)      ");
			sb.append("    join EmatchExternalSystemStatus eess on(te.AllocationStatusId = eess.ExternalSystemStatusId and eess.StatusCategoryId <> 'C')      ");
			sb.append("    join EmatchClearingStatus ecs on(te.ClearingStatusId = ecs.ClearingStatusId and ecs.StatusCategoryId <> 'C')      ");
			sb.append("    where      ");
			sb.append("    th.ExternalSystemId = "+formBean.getExternalSystemId()+"      ");
			sb.append("    and th.ClientId = "+formBean.getClientId()          );
			sb.append("    and th.ExternalSystemTradeId in ("+ids+") ");
			
			//List<Object> resultList=CommonSQLExecuterDAO.getInstance().executeSQL(sb.toString());
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + sb.toString(), ListDTO.class);
			if(CommonUtil.isNull(listDTO) || CommonUtil.isNullOrEmpty(listDTO.getStringList())){
				return null;
			}
			
			for(int i=0; i<listDTO.getStringList().size(); i++){
				list.add(String.valueOf(listDTO.getStringList().get(i)));
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public boolean isGoConfirmTrade(String confirmMethod) {
		List<DashBoardDTO> confirmMethods = getConfirmMethod();
		for (DashBoardDTO dashBoardDTO : confirmMethods) {
			if(dashBoardDTO.getDescription().trim().equalsIgnoreCase(confirmMethod)){
				return true;
			}
		}
		return false;
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, String> getSeverityMap(){
		Map<String, String> severltyMap = null;
		String sql =    "select ltrim(rtrim(substring(pvp.Value, 1, charindex('_', pvp.Value)-1))) as ParameterName, convert(smallint,substring(pvp.Value, charindex('_', pvp.Value)+1, char_length(pvp.Value))) as ParameterValue from ConfParamValuePair pvp join ConfParameter pm on (pvp.ParameterId = pm.ParameterId) join ConfParameterType pt on (pm.ParameterTypeId = pt.ParameterTypeId) where pt.ShortName = 'SeverityMapping'";
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			severltyMap = new HashMap<String, String>();
			for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
				severltyMap.put(String.valueOf(((Object[])listDTO.getObjectArrayList().get(i))[0]).replaceAll("\\s", ""), String.valueOf(((Object[])listDTO.getObjectArrayList().get(i))[1]));
			}		
		}
        return severltyMap;
	}
	
	public SeverityDTO getSeverityIdByStatus(String eventStatusName) {
		String id = getSeverityMap().get(eventStatusName.replaceAll("\\s", "").toUpperCase());
		return SeverityDTO.getInstance(Integer.valueOf(id));
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<String> getGCCompletedStatusIds(){
		List<String> completedStatusIds = null;
		String sql =    "SELECT pv.Value FROM ConfParameter pm (INDEX PK_ConfParameter) JOIN ConfParamValuePair pv (INDEX IX_ConfParamValuePair_ParamId) ON (pm.ParameterId = pv.ParameterId) JOIN ConfParameterType pt (INDEX PK_ConfParameterType) ON (pm.ParameterTypeId = pt.ParameterTypeId) WHERE pt.ShortName = 'EMIRCompletedStatus'";
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getStringList())){
			completedStatusIds = new ArrayList<String>();
			for(int i=0; i<listDTO.getStringList().size(); i++){
				String str = String.valueOf(listDTO.getStringList().get(i));
				completedStatusIds.add(str.trim().substring(str.indexOf("_")+1));
			}		
		}
        return completedStatusIds;
	}
	
	public boolean checkConfirmMethod(Short dbId, String confirmMethodName) {
		List<DashBoardDTO> confirmMethods = getConfirmMethod();
		for (DashBoardDTO dashBoardDTO : confirmMethods) {
			if(dashBoardDTO.getId() == dbId && dashBoardDTO.getDescription().equalsIgnoreCase(confirmMethodName)){
				return true;
			}
		}		
		return false;
	}
	
	public boolean checkElectronicConfirmMethod(String confirmMethodName) {
		List<DashBoardDTO> confirmMethods = getElectronicConfirmMethod();
		for (DashBoardDTO dashBoardDTO : confirmMethods) {
			if(dashBoardDTO.getDescription().equalsIgnoreCase(confirmMethodName)){
				return true;
			}
		}		
		return false;
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, ConfExternalSystemStatusDTO> getConfExternalSystemStatus(){
		Map<String, ConfExternalSystemStatusDTO> confExternalSystemMAP = null;
		String sql =    "SELECT ExternalSystemStatusId = ce.ExternalSystemStatusId,ExternalSystemId = e.ExternalSystemId,ExternalSystemName = e.ShortName,ShortName = ce.ShortName,Description = ce.Description,LastModifiedDateTime = ce.LastModifiedDateTime, ActualStatusId = ce.ActualStatusId FROM ConfExternalSystemStatus ce, ExternalSystem e WHERE ce.ExternalSystemId = e.ExternalSystemId  ORDER BY ExternalSystemStatusId";
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			confExternalSystemMAP = new HashMap<String, ConfExternalSystemStatusDTO>();
			for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
				Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
				
				ConfExternalSystemStatusDTO statusDTO = new ConfExternalSystemStatusDTO();
				statusDTO.setExternalSystemStatusId(Integer.valueOf(String.valueOf(rowData[0])));
				statusDTO.setExternalSystemId(Integer.valueOf(String.valueOf(rowData[1])));
				statusDTO.setExternalSystemName(String.valueOf(rowData[2]));
				statusDTO.setShortName(String.valueOf(rowData[3]));
				statusDTO.setDescription(String.valueOf(rowData[4]));
				statusDTO.setLastModifiedDateTime(String.valueOf(rowData[5]));
				statusDTO.setActualStatusId(Integer.valueOf(String.valueOf(rowData[6])));
				
				confExternalSystemMAP.put(statusDTO.getShortName(), statusDTO);
			}		
		}
        return confExternalSystemMAP;
	}
	
	public ConfExternalSystemStatusDTO getExternalSystemStatusIdByShortName(String shortName){
		return getConfExternalSystemStatus().get(shortName);
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, AliasTypeDTO> getConfAliasType(){
		Map<String, AliasTypeDTO> aliasTypeMAP = null;
		String sql =    "SELECT AliasTypeId, ShortName, Description from AliasType where ShortName like 'CONF_%'";
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			aliasTypeMAP = new HashMap<String, AliasTypeDTO>();
			for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
				Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
				
				AliasTypeDTO aliasTypeDTO = new AliasTypeDTO();
				aliasTypeDTO.setId(Integer.valueOf(String.valueOf(rowData[0])));
				aliasTypeDTO.setShortName(String.valueOf(rowData[1]));
				aliasTypeDTO.setDescription(String.valueOf(rowData[2]));
				
				aliasTypeMAP.put(aliasTypeDTO.getShortName().trim(), aliasTypeDTO);
			}		
		}
        return aliasTypeMAP;
	}
	
	public AliasTypeDTO getConfAliasTypeIdByName(String shortName){
		return getConfAliasType().get(shortName);
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<CustomTradeTypeDTO> getCustomTradeTypes() {
		return clientServiceManager.findCustomTradeType();
	}
	
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String, Map<String, String>> getDTCCAccessMode(){
		Map<String, Map<String, String>> dtccAccessModeMap = null;
		String sql =    "SELECT fp.Conf_FundProductTypeId,fp.FundId,fp.ProductTypeId FROM Conf_FundProductType fp JOIN Fund f ON (fp.FundId = f.FundId) JOIN Client c ON (f.ClientId = c.ClientId) where fp.DTCCAccessMode in ('R','S')";
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			dtccAccessModeMap = new HashMap<String, Map<String, String>>();
			for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
				Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
				HashMap<String, String> dtccInnerMap = new HashMap<String, String>();
				dtccInnerMap.put(String.valueOf(rowData[1]).trim(), String.valueOf(rowData[2]).trim());				
				dtccAccessModeMap.put(String.valueOf(rowData[0]), dtccInnerMap);
			}		
		}
        return dtccAccessModeMap;
	}
	
	public boolean isTradeDTCC(GoConfirmDashBooardItemTradeDTO dto,boolean flag){	
		Map<String, Map<String, String>> dtccAccessModeMap = getDTCCAccessMode();
		Map<String, String> isDtccMap  = dtccAccessModeMap.get(dto.getFundId()); 

		if(isDtccMap!=null && ("S").equalsIgnoreCase(isDtccMap.get(dto.getTradeTypeId()))){
			flag = true;
		}
		return flag;
	}
	
	public boolean isBilateralTrade(String centralClearingId)	{
		if (centralClearingId!=null){
			if (centralClearingId.equalsIgnoreCase("") || centralClearingId.equalsIgnoreCase("0")
					|| centralClearingId.equalsIgnoreCase("8") || centralClearingId.equalsIgnoreCase("30")){
				return true ;  /* bilateral trade */
			} else {
				return false ;  /* cleared trade */
			}
		}
		return false;
	}
	
	public Integer getAliasTypeId(AliasType aliasType){
		switch (aliasType) {
		case CPTYDOCDEALID:
			return 8;
		case DTCCDEALID:
			return 10;
		case SWAPSWIREDEALID:
			return 9;
		case CLEARINGNETTINGID:
			return 2383;
		case CLEARINGHOUSEREFID:
			return 2384;
		case CLEARINGSTATUS:
			return 2386;
		case UNIVERSALSWAPSID:
			return getConfAliasTypeIdByName("CONF_USIID").getId();
		case USIPREFIX:
			return getConfAliasTypeIdByName("CONF_USIPREFIX").getId();
		case UTIPREFIX:
			return getConfAliasTypeIdByName("CONF_UTIPREFIX").getId();
		case UTIVALUE:
			return getConfAliasTypeIdByName("CONF_UTIID").getId();
		case EXEVENUE:
			return getConfAliasTypeIdByName("CONF_EXECVENUE").getId();
		case EXEDATE:
			return getConfAliasTypeIdByName("CONF_EXECDATE").getId();
		case CLRDATE:
			return getConfAliasTypeIdByName("CONF_CLEAREDDATE").getId();
		default:
			break;
		}
		return null;		
	}
	
	public String getClearingStatus(String clearingStatusId){
		return StaticValuesUtil.getClearingEditStatType().get(clearingStatusId);
	}
	
	public boolean isCOMClient(String fundId, String tradeTypeId){
		try {
			Map<String, Map<String, String>> dtccAccessModeMap = getDTCCAccessMode();
			Map<String, String> isDtccMap = dtccAccessModeMap.get(fundId);
			if(!CommonUtil.isNull(isDtccMap)){
				String dtccAccessMode = isDtccMap.get(tradeTypeId);
				if(!CommonUtil.isNullOrEmpty(dtccAccessMode) && ("R").equalsIgnoreCase(dtccAccessMode)){
					return true;
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "isCOMClient", e);
		}
		return false;
	}
	
	public List<ConfDashBoardItemDTO> getConfDashBoardItemByDBId(Long dashBoardItemId){
		List<ConfDashBoardItemDTO> coItemDTOs = null;
		try {
			coItemDTOs = new ArrayList<ConfDashBoardItemDTO>();
			String sql =    "SELECT db.*, es.ShortName as ExternalSystemStatusName, f.DerivServParticipantId as FundParticipant FROM ConfDashBoardItem db JOIN ConfExternalSystemStatus es ON (db.ExternalSystemStatusId = es.ExternalSystemStatusId) JOIN DashBoardItem d ON (db.DashBoardItemId = d.DashBoardItemId) JOIN Fund f ON (d.FundId = f.FundId) WHERE db.DashBoardItemId = "+dashBoardItemId;
			//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
					Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
					
					ConfDashBoardItemDTO dto = new ConfDashBoardItemDTO();
					dto.setDashBoardItemId(Long.valueOf(String.valueOf(rowData[0])));
					dto.setExternalSystemStatusId(Integer.valueOf(String.valueOf(rowData[1])));
					dto.setLastModifiedUserId(Integer.valueOf(String.valueOf(rowData[2])));
					dto.setLastModifiedDateTime(Timestamp.valueOf(String.valueOf(rowData[3])));
					dto.setTradeRefNumber(String.valueOf(rowData[4]));
					dto.setTradeRefSupplement(String.valueOf(rowData[5]));
					dto.setExternalSystemStatusName(String.valueOf(rowData[6]));
					dto.setFundParticipant(String.valueOf(rowData[7]));
					
					coItemDTOs.add(dto);
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getConfDashBoardItemByDBId", e);
		}
        return coItemDTOs;
	}
	
	public int persistConfDashBoardItem(ConfDashBoardItemDTO confDashBoardItemDTO) {
		try {
			if (exists(confDashBoardItemDTO)){
				return update(confDashBoardItemDTO);
			}else{
				return insert(confDashBoardItemDTO);
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "persistConfDashBoardItem", e);
		}		
		return 0;
	}
	
	private int update(ConfDashBoardItemDTO confDashBoardItemDTO) {
		int val = 0;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ConfDashBoardItem SET ExternalSystemStatusId = ")
						.append(confDashBoardItemDTO.getExternalSystemStatusId())
						.append(", LastModifiedUserId = ")
						.append(confDashBoardItemDTO.getLastModifiedUserId())
						.append(", LastModifiedDateTime = getdate(), TradeRefNumber = ")
						.append(confDashBoardItemDTO.getTradeRefNumber())
						.append(", TradeRefSupplement = ")
						.append(confDashBoardItemDTO.getTradeRefSupplement())
						.append("WHERE DashBoardItemId = ? ");
			//val = CommonSQLExecuterDAO.getInstance().updateSQL(sql.toString());
			val = restTemplate.getForObject(matchingServiceURL + "/executeUpdateSQL.go?query=" + sql.toString(), Integer.class);
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "update", e);
		}
		return val;
	}

	private int insert(ConfDashBoardItemDTO confDashBoardItemDTO) {
		int val = 0;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO ConfDashBoardItem(DashBoardItemId,ExternalSystemStatusId, LastModifiedUserId, LastModifiedDateTime, TradeRefNumber, TradeRefSupplement) VALUES (")
						.append(confDashBoardItemDTO.getDashBoardItemId())
						.append(", ")
						.append(confDashBoardItemDTO.getExternalSystemStatusId())
						.append(", ")
						.append(confDashBoardItemDTO.getLastModifiedUserId())
						.append(", getdate(), ")
						.append(confDashBoardItemDTO.getTradeRefNumber())
						.append(", ")
						.append(confDashBoardItemDTO.getTradeRefSupplement())
						.append(") ");

			//val = CommonSQLExecuterDAO.getInstance().updateSQL(sql.toString());
			val = restTemplate.getForObject(matchingServiceURL + "/executeUpdateSQL.go?query=" + sql.toString(), Integer.class);
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "insert", e);
		}
		return val;
	}

	private boolean exists(ConfDashBoardItemDTO confDashBoardItemDTO) {
		List<ConfDashBoardItemDTO> dtos = getConfDashBoardItemByDBId(confDashBoardItemDTO.getDashBoardItemId());
		if (!CommonUtil.isNullOrEmpty(dtos)){
			return true;
		}		
		return false;
	}
	
	public List<DashBoardItemAuditDTO> findDashBoardItemAllVersions(String dashBoardItemId){
		return clientServiceManager.findDashBoardItemAllVersions(new BigDecimal(dashBoardItemId));
	}
	
	public ResponseDTO getAuditResponseDTO(String dashBoardItemId, String auditTab){
		ResponseDTO responseDTO = null;
		try {
			List<JQgridDTO> rowData = new ArrayList<JQgridDTO>();
			List<DashBoardItemAuditDTO> auditList = findDashBoardItemAllVersions(dashBoardItemId);
			Collections.reverse(auditList);
			List<List<String>> reslutSet = new ArrayList<List<String>>();
			JQgridDTO dto = null;		
			switch (auditTab) {
			case "Comments":
				List<NoteDTO> notes = auditList.get(0).getNotes();
				if(!CommonUtil.isNull(notes) && notes.size()>0){
					for (NoteDTO noteDTO : notes) {
						List<String> tempList = new ArrayList<String>();
						tempList.add(String.valueOf(dashBoardItemId));
						tempList.add(DateUtil.getDateFromTimeStamp(noteDTO.getCreatedDateTime(), "yyyy-MM-dd HH:mm:ss"));
						tempList.add(String.valueOf(noteDTO.getDashBoardItemStatusShortName()));
						tempList.add(CommonUtil.addHTMLXMPTag(String.valueOf(noteDTO.getNote())));
						tempList.add(String.valueOf(noteDTO.getLastModifiedUserShortName()));
						reslutSet.add(tempList);
					}
				}
				responseDTO = new ResponseDTO();
				responseDTO.setTotal(String.valueOf(reslutSet.size()));
				responseDTO.setPage("1");
				responseDTO.setRecords("80");
				responseDTO.setTotalPages("1");
				responseDTO.setRows(reslutSet);
				responseDTO.setColModel(StaticValuesUtil.getGCAuditCommentsHeader());
				break;

			case "DTCC_AUDIT":
				//List<Object> dtccAuditList = commonSQLExecuterDAO.executeSQL(SELECT_DTCC_AUDIT_BY_DBID.replace("?", dashBoardItemId));
				ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + SELECT_DTCC_AUDIT_BY_DBID.replace("?", dashBoardItemId), ListDTO.class);
				if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
					for (Object[] data : listDTO.getObjectArrayList()) {
						//Object[] data = (Object[])rowObj;
						List<String> dataList = new ArrayList<String>();
						dataList.add(CommonUtil.getStringValue(data[0]));
						dataList.add(CommonUtil.getStringValue(data[3]));
						String action = CommonUtil.getStringValue(data[2]);
						if("U".equalsIgnoreCase(action)){
							dataList.add("Update");
						}else{
							dataList.add("Insert");
						}
						dataList.add(CommonUtil.getStringValue(data[4]));
						dataList.add(CommonUtil.getStringValue(data[1]));
						dataList.add(CommonUtil.getStringValue(data[5]));
						dataList.add(CommonUtil.getStringValue(data[6]));
						dataList.add(CommonUtil.getStringValue(data[7]));
						dataList.add(CommonUtil.getStringValue(data[8]));
						reslutSet.add(dataList);
					}


					for (int i = 0; i < reslutSet.size(); i++) {
						List<String> temp = reslutSet.get(i);
						dto = new JQgridDTO();
						dto.setId(Long.valueOf(i+1));
						dto.setCell(temp);
						rowData.add(dto);
					}
					responseDTO = new ResponseDTO();
					responseDTO.setTotal(String.valueOf(reslutSet.size()));
					responseDTO.setPage("1");
					responseDTO.setRecords("80");
					responseDTO.setTotalPages("1");
					responseDTO.setRowData(new Gson().toJson(rowData));
					responseDTO.setColModel(StaticValuesUtil.getDTCCAuditHeader());
				}
				break;	

			default:
				for (int i = 0; i < auditList.size(); i++) {
					String previousVal="", currentVal="";
					DashBoardItemAuditDTO dbIAuditDTO = (DashBoardItemAuditDTO) auditList.get(i);
					DashBoardItemAuditDTO dbIAuditDTONext = null;
					List<String> tempList = null;
					switch (auditTab) {
					case "System":
						tempList = getSystemAuditDetails(dbIAuditDTO);
						break;

					case "Type":
						if(i+1<auditList.size()){
							dbIAuditDTONext = (DashBoardItemAuditDTO) auditList.get(i+1);
							if(!dbIAuditDTO.getDashBoardItemTypeShortName().trim().equalsIgnoreCase(dbIAuditDTONext.getDashBoardItemTypeShortName().trim())){
								previousVal = dbIAuditDTONext.getDashBoardItemTypeShortName().trim();
								currentVal = dbIAuditDTO.getDashBoardItemTypeShortName().trim();
							}else{
								previousVal = null;
								currentVal = null;
							}
						}else{
							previousVal = "";
							currentVal = dbIAuditDTO.getDashBoardItemTypeShortName().trim();
						}

						tempList = getTypeAuditDetails(dbIAuditDTO, previousVal, currentVal);
						break;

					case "Status":
						if(i+1<auditList.size()){
							dbIAuditDTONext = (DashBoardItemAuditDTO) auditList.get(i+1);
							if(!dbIAuditDTO.getDashBoardItemStatusShortName().trim().equalsIgnoreCase(dbIAuditDTONext.getDashBoardItemStatusShortName().trim())){
								previousVal = dbIAuditDTONext.getDashBoardItemStatusShortName().trim();
								currentVal = dbIAuditDTO.getDashBoardItemStatusShortName().trim();
							}else{
								previousVal = null;
								currentVal = null;
							}
						}else{
							previousVal = "";
							currentVal = dbIAuditDTO.getDashBoardItemStatusShortName().trim();
						}

						tempList = getTypeAuditDetails(dbIAuditDTO, previousVal, currentVal);
						break;

					case "Method":
						if(i+1<auditList.size()){
							dbIAuditDTONext = (DashBoardItemAuditDTO) auditList.get(i+1);
							if(!dbIAuditDTO.getDashBoardShortName().trim().equalsIgnoreCase(dbIAuditDTONext.getDashBoardShortName().trim())){
								previousVal = dbIAuditDTONext.getDashBoardShortName().trim();
								currentVal = dbIAuditDTO.getDashBoardShortName().trim();
							}else{
								previousVal = null;
								currentVal = null;
							}
						}else{
							previousVal = "";
							currentVal = dbIAuditDTO.getDashBoardShortName().trim();
						}

						tempList = getTypeAuditDetails(dbIAuditDTO, previousVal, currentVal);
						break;

					default:
						break;
					}

					if(!CommonUtil.isNull(tempList) && tempList.size()>0){
						reslutSet.add(tempList);
					}
				}

				for (int i = 0; i < reslutSet.size(); i++) {
					List<String> temp = reslutSet.get(i);
					dto = new JQgridDTO();
					dto.setId(Long.valueOf(i+1));
					dto.setCell(temp);
					rowData.add(dto);
				}

				responseDTO = new ResponseDTO();
				switch (auditTab) {
				case "System":
					responseDTO.setColModel(StaticValuesUtil.getGCAuditSystemHeader());
					break;
				case "Comments":
					responseDTO.setColModel(StaticValuesUtil.getGCAuditCommentsHeader());
					break;
				case "Type":
				case "Status":
				case "Method":
					responseDTO.setColModel(StaticValuesUtil.getGCAuditTypeHeader());
					break;
				case "DTCC_AUDIT":
					responseDTO.setColModel(StaticValuesUtil.getDTCCAuditHeader());
					break;

				default:
					break;
				}

				responseDTO.setTotal(String.valueOf(reslutSet.size()));
				responseDTO.setPage("1");
				responseDTO.setRecords("80");
				responseDTO.setTotalPages("1");
				responseDTO.setRowData(new Gson().toJson(rowData));
				break;
			}

		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getAuditResponseDTO", e);
		}
		return responseDTO;
	}

	private List<String> getTypeAuditDetails(DashBoardItemAuditDTO dbIAuditDTO, String previousVal, String currentVal) {
		List<String> tempList = null;
		if(!CommonUtil.isNullOrEmpty(previousVal) || !CommonUtil.isNullOrEmpty(currentVal)){
			tempList = new ArrayList<String>();
			tempList.add(String.valueOf(dbIAuditDTO.getId()));
			tempList.add(DateUtil.getDateFromTimeStamp(dbIAuditDTO.getLastModifiedDateTime(), "yyyy-MM-dd HH:mm:ss"));
			tempList.add(previousVal);
			tempList.add(currentVal);
			tempList.add(String.valueOf(dbIAuditDTO.getLastModifiedUser().getShortName()));
		}
		return tempList;
	}

	private List<String> getSystemAuditDetails(DashBoardItemAuditDTO dbIAuditDTO) {
		List<String> tempList = new ArrayList<String>();
		tempList .add(String.valueOf(dbIAuditDTO.getId()));
		tempList.add(DateUtil.getDateFromTimeStamp(dbIAuditDTO.getAuditDateTime(), "yyyy-MM-dd HH:mm:ss"));
		tempList.add(String.valueOf(dbIAuditDTO.getAuditActionId().equals("I") ? "Insert" : "Update"));
		tempList.add(String.valueOf(dbIAuditDTO.getDashBoardItemTypeShortName()));
		tempList.add(String.valueOf(dbIAuditDTO.getDashBoardItemStatusShortName()));
		tempList.add(String.valueOf(dbIAuditDTO.getDashBoardShortName()));
		tempList.add(DateUtil.getDateFromTimeStamp(dbIAuditDTO.getLastStatusChangeDateTime(), "yyyy-MM-dd HH:mm:ss"));
		tempList.add(String.valueOf(dbIAuditDTO.getLastModifiedUser().getShortName()));
		return tempList;
	}
	
	public Map<String, List<DashBoardItemStatusDTO>> getConfirmStatusMap() {
		if (CommonUtil.isNullOrEmpty(confirmStatusMap)) {
			confirmStatusMap = new HashMap<>();
			List<DashBoardItemStatusDTO> confimStatusList = getConfirmStatus();
			List<DashBoardItemStatusDTO> tempList = new ArrayList<>();
			for (DashBoardItemStatusDTO dbisDTO : confimStatusList) {
				if (StaticValuesUtil.getNonSTPConfirmStatusList().contains(dbisDTO.getShortName())) {
					tempList.add(dbisDTO);
				}
			}
			confirmStatusMap.put(SearchModeId.NON_STP, tempList);

			List<DashBoardItemStatusDTO> tempList1 = new ArrayList<>();
			for (DashBoardItemStatusDTO dbisDTO : confimStatusList) {
				if (StaticValuesUtil.getOTCConfirmStatusList().contains(dbisDTO.getShortName())) {
					tempList1.add(dbisDTO);
				}
			}
			confirmStatusMap.put(SearchModeId.NON_STP_OTC, tempList1);

			List<DashBoardItemStatusDTO> tempList2 = new ArrayList<>();
			for (DashBoardItemStatusDTO dbisDTO : confimStatusList) {
				if (StaticValuesUtil.getNonOTCConfirmStatusList().contains(dbisDTO.getShortName())) {
					tempList2.add(dbisDTO);
				}
			}
			confirmStatusMap.put(SearchModeId.NON_STP_NON_OTC, tempList2);
		}
			
		return confirmStatusMap;
	}
	
	public Map<String, List<DashBoardDTO>> getConfirmMethodMap() {
		if (CommonUtil.isNullOrEmpty(confirmMethodMap)) {
			confirmMethodMap = new HashMap<>();
			List<DashBoardDTO> confimMethods = getConfirmMethod();
			confirmMethodMap.put(SearchModeId.NON_STP, confimMethods);
			
			List<DashBoardDTO> otcConfimMethods = new ArrayList<>(confimMethods);
			for (Iterator<DashBoardDTO> iterator = otcConfimMethods.iterator(); iterator.hasNext();) {
				DashBoardDTO dashBoardDTO = iterator.next();
				if (ConfirmMethod.MAIL.equalsIgnoreCase(dashBoardDTO.getShortName())) {
					iterator.remove();
				}
			}
			confirmMethodMap.put(SearchModeId.NON_STP_OTC, otcConfimMethods);
			
			List<DashBoardDTO> nonOtcConfimMethods = new ArrayList<>();
			for (DashBoardDTO dashBoardDTO : confimMethods) {
				switch (dashBoardDTO.getShortName()) {
				case ConfirmMethod.PAPER:
				case ConfirmMethod.PORTAL:
					nonOtcConfimMethods.add(dashBoardDTO);
					break;
				}
			}
			confirmMethodMap.put(SearchModeId.NON_STP_NON_OTC, nonOtcConfimMethods);
		}
			
		return confirmMethodMap;
	}
	
	public Map<String, List<DashBoardItemTypeDTO>> getConfirmTypeMap() {
		if (CommonUtil.isNullOrEmpty(confirmTypeMap)) {
			confirmTypeMap = new HashMap<>();
			List<DashBoardItemTypeDTO> confirmTypeList = getConfirmType();
			confirmTypeMap.put(SearchModeId.NON_STP, confirmTypeList);
			
			List<DashBoardItemTypeDTO> nonOtcConfirmTypeList = new ArrayList<>();
			for (DashBoardItemTypeDTO dbitDTO : confirmTypeList) {
				switch (dbitDTO.getShortName()) {
				case "ASSIGNMENT":
				case "ORIGINAL":
				case "PARTICIPATION":
				case "TERMINATION":
					nonOtcConfirmTypeList.add(dbitDTO);
					break;
				}
			}
			confirmTypeMap.put(SearchModeId.NON_STP_NON_OTC, nonOtcConfirmTypeList);

			//List<DashBoardItemTypeDTO> otcConfirmTypeList = new ArrayList<>(confirmTypeList);
			for (Iterator<DashBoardItemTypeDTO> iterator = confirmTypeList.iterator(); iterator.hasNext();) {
				DashBoardItemTypeDTO dbitDTO = (DashBoardItemTypeDTO) iterator.next();
				if ("PARTICIPATION".equals(dbitDTO.getShortName())) {
					iterator.remove();
				}
			}
			confirmTypeMap.put(SearchModeId.NON_STP_OTC, confirmTypeList);
			
		}
			
		return confirmTypeMap;
	}


	/**
	 * hverma..10:27:07 PM..@param dbiIds
	 * hverma..10:27:07 PM..@param request
	 * @return 
	 * @throws GomatchBaseException 
	 * @throws SQLException 
	 */

	public void sendMessageToDTCC(List<Long> dbiIds, HttpServletRequest request) throws GomatchBaseException, SQLException {
		try {
			List<String> sqlList=new ArrayList<String>();
			Integer externalSystemId = GoMatchConstants.ExternalSystem_CONFIRMATIONSYSTEM.DTCC;
			for(long dbiId : dbiIds){
				/** Prepare ConfMessageLog data **/
				ConfMessageLogDTO mdto = new ConfMessageLogDTO();
				mdto.setActivityId(getConfActivityKey("Modify",externalSystemId));
				mdto.setTrackerStatusId(getConfTrackerStatus("NEW",externalSystemId));//ConfTrackerStatus
				mdto.setDashBoardItemId(String.valueOf(dbiId));
				mdto.setExternalSystemId(externalSystemId);			
				sqlList.add(getConfMsgLogInsertQuery(mdto));
				
				/** Prepare ConfDashBoardItem data **/
				List<ConfDashBoardItemDTO> confdbiDtos =  getConfDashBoardItemByDBId(dbiId);
				ConfDashBoardItemDTO confDashBoardItemDTO=null;
				if(!CommonUtil.isNullOrEmpty(confdbiDtos)){
				  confDashBoardItemDTO = confdbiDtos.get(0);
				}
				if(confDashBoardItemDTO==null){
					confDashBoardItemDTO=new ConfDashBoardItemDTO();
				}
				
				confDashBoardItemDTO.setDashBoardItemId(dbiId);
				confDashBoardItemDTO.setExternalSystemStatusId(getConfExternalSystemUniqueKey("Submit", externalSystemId));
				UserDTO userDTO = SessionUtil.getUserDTO(request);
				confDashBoardItemDTO.setLastModifiedUserId(userDTO.getId());
				sqlList.add(getConfdbiPersistQuery(confDashBoardItemDTO));
				
				/** perform db operation after both the object ready to persist**/
				if(sqlList.size()>0){
					ListDTO listDTO = new ListDTO();
					listDTO.setStringList(sqlList);
					Boolean flag = restTemplate.postForObject(matchingServiceURL + "/batchExecuteUpdateSQL.go", listDTO , Boolean.class);
					//commonSQLExecuterDAO.getInstance().multiUpdate(sqlList);
				}
			}
		} catch (GomatchBaseException e) {
			GomatchExceptionHandler.getInstance().logAndThrow(GomatchErrorDetail.getInstance("01","GMTService - sendMessageToDTCC",e));
		}
	}
	
	/**
	 * hverma..9:14:58 PM..@param string
	 * hverma..9:14:58 PM..@param externalSystemId
	 * hverma..9:14:58 PM..@return
	 */
	public Integer getConfExternalSystemUniqueKey(String shortName, long externalSystemId){
		Integer ConfExternalSystemId = 0 ;
		try {
			String FIND_BY_UNIQUE_KEY = "SELECT * FROM ConfExternalSystemStatus WHERE ShortName = '"+shortName+"' AND ExternalSystemId = "+externalSystemId;
			//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(FIND_BY_UNIQUE_KEY);
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + FIND_BY_UNIQUE_KEY, ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
					Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
					ConfExternalSystemId=Integer.valueOf(String.valueOf(rowData[0]));
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "ConfExternalSystemStatusId", e);
		}
        return ConfExternalSystemId;
	}

	/**
	 * hverma..9:53:35 PM..@param string
	 * hverma..9:53:35 PM..@param externalSystemId
	 * hverma..9:53:35 PM..@return
	 */
	public int getConfTrackerStatus(String shortName, Integer externalSystemId) {
		Map<String,ConfTrackerStatusDTO> trackerStatusMap = getConfTrackerStatusDTO();
		return trackerStatusMap.get(shortName+"_"+externalSystemId).getTrackerStatusId();
	}

	/**
	 * hverma..9:59:00 PM..@return
	 */
		@NonLoggable
		@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
		private Map<String, ConfTrackerStatusDTO> getConfTrackerStatusDTO() {
			Map<String,ConfTrackerStatusDTO> trackerStatusMap=new HashMap<String,ConfTrackerStatusDTO>();
			try {
				String sql="SELECT TrackerStatusId = ct.TrackerStatusId,ExternalSystemId = e.ExternalSystemId,ExternalSystem = e.ShortName,ShortName = ct.ShortName,Description = ct.Description,LastModifiedDateTime = ct.LastModifiedDateTime FROM ConfTrackerStatus ct , ExternalSystem e WHERE ct.ExternalSystemId = e.ExternalSystemId ORDER BY ct.TrackerStatusId";
				//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
				ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
				if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
					for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
						Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
	
						ConfTrackerStatusDTO trackerStatusDto = new ConfTrackerStatusDTO();
						trackerStatusDto.setTrackerStatusId(Integer.valueOf(String.valueOf(rowData[0])));
						trackerStatusDto.setExternalSystemId(Integer.valueOf(String.valueOf(rowData[1])));
						trackerStatusDto.setExternalSystemName(String.valueOf(rowData[2]));
						trackerStatusDto.setShortName(String.valueOf(rowData[3]));
						trackerStatusDto.setDescription(String.valueOf(rowData[4]));
						trackerStatusDto.setLastModifiedDateTime(String.valueOf(rowData[5]));
						trackerStatusMap.put(trackerStatusDto.getShortName()+"_"+trackerStatusDto.getExternalSystemId(), trackerStatusDto);
					}
				}
			} catch (Exception e) {
				CommonUtil.loggerErrorMessage(log, "getConfActivityDTO()", e);
			}
			return trackerStatusMap;
		}

	/**
	 * hverma..8:16:53 PM..@param string
	 * hverma..8:16:53 PM..@param i
	 * hverma..8:16:53 PM..@return
	 */
	
	public int getConfActivityKey(String shortName, int externalSysId) {
		Map<String,ConfActivityDTO> activityMap = getConfActivityDTO();
		return activityMap.get(shortName+"_"+externalSysId).getActivityId();
	}
	
	/**
	 * hverma..9:59:00 PM..@return
	 */
	@NonLoggable
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String,ConfActivityDTO> getConfActivityDTO(){
		Map<String,ConfActivityDTO> confActivityMap=new HashMap<String,ConfActivityDTO>();
		try {
			String sql="SELECT ActivityId = ca.ActivityId, ShortName = ca.ShortName, Description = ca.Description, LastModifiedDateTime = ca.LastModifiedDateTime, ExternalSystemId   = e.ExternalSystemId, ExternalSystemName = e.ShortName FROM ConfActivity ca, ExternalSystem e WHERE ca.ExternalSystemId = e.ExternalSystemId ORDER BY ActivityId ";
			//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0; i<listDTO.getObjectArrayList().size(); i++){
					Object[] rowData = (Object[]) listDTO.getObjectArrayList().get(i);
	
					ConfActivityDTO activityDto = new ConfActivityDTO();
					activityDto.setActivityId(Integer.valueOf(String.valueOf(rowData[0])));
					activityDto.setShortName(String.valueOf(rowData[1]));
					activityDto.setDescription(String.valueOf(rowData[2]));
					activityDto.setLastModifiedDateTime(String.valueOf(rowData[3]));
					activityDto.setExternalSystemId(Integer.valueOf(String.valueOf(rowData[4])));
					activityDto.setExternalSystemName(String.valueOf(rowData[5]));
					confActivityMap.put(activityDto.getShortName()+"_"+activityDto.getExternalSystemId(), activityDto);
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getConfActivityDTO()", e);
		}
		return confActivityMap;
	}
	
	
	/**
	 * hverma..7:03:30 PM..@param confDashBoardItemDTO
	 * hverma..7:03:30 PM..@return
	 */
	private String getConfdbiPersistQuery(ConfDashBoardItemDTO confDashBoardItemDTO) throws GomatchBaseException{
		StringBuffer sql = new StringBuffer();
		if(!exists(confDashBoardItemDTO)){
			sql.append("INSERT INTO ConfDashBoardItem(DashBoardItemId,ExternalSystemStatusId, LastModifiedUserId, LastModifiedDateTime, TradeRefNumber, TradeRefSupplement) VALUES (")
			.append(confDashBoardItemDTO.getDashBoardItemId())
			.append(", ")
			.append(confDashBoardItemDTO.getExternalSystemStatusId())
			.append(", ")
			.append(confDashBoardItemDTO.getLastModifiedUserId())
			.append(", getdate(), ")
			.append(confDashBoardItemDTO.getTradeRefNumber())
			.append(", ")
			.append(confDashBoardItemDTO.getTradeRefSupplement())
			.append(") ");
		}else{
			sql.append("UPDATE ConfDashBoardItem SET ExternalSystemStatusId = ")
			.append(confDashBoardItemDTO.getExternalSystemStatusId())
			.append(", LastModifiedUserId = ")
			.append(confDashBoardItemDTO.getLastModifiedUserId())
			.append(", LastModifiedDateTime = getdate(), TradeRefNumber = '")
			.append(confDashBoardItemDTO.getTradeRefNumber()).append("'");
			
			if(!CommonUtil.isNullOrEmpty(confDashBoardItemDTO.getTradeRefSupplement())){
				sql.append(",").append(" TradeRefSupplement = ")
				.append(confDashBoardItemDTO.getTradeRefSupplement());
			}
			
			sql.append(" WHERE DashBoardItemId =  ")
			.append(confDashBoardItemDTO.getDashBoardItemId());
		}
		return sql.toString();
	}

	/**
	 * hverma..5:22:45 PM..@param mdto
	 * hverma..5:22:45 PM..@return
	 * @throws SQLException 
	 */
	public String getConfMsgLogInsertQuery(ConfMessageLogDTO confMsgLogdto) throws GomatchBaseException, SQLException{
			StringBuffer sql = new StringBuffer();
			long messageId=getConfObjectQueryDAO().getNextId("ConfMessageLog");
			sql.append("insert into ConfMessageLog (MessageId,ActivityId,TrackerStatusId,DashBoardItemId,ExternalSystemId,CreatedDateTime,LastModifiedDateTime,Comments,FundParticipantId,ContraParticipantId,ContraTradeReference,ProductTypeId,TransactionTypeId,ContraTradeSupplement) VALUES (")
						.append(messageId)
						.append(", ")
						.append(confMsgLogdto.getActivityId())
						.append(", ")
						.append(confMsgLogdto.getTrackerStatusId())
						.append(", ")
						.append(confMsgLogdto.getDashBoardItemId())
						.append(", ")
						.append(confMsgLogdto.getExternalSystemId())
						.append(", getdate() ")
						.append(", getdate() ")
						.append(", ")
						.append(confMsgLogdto.getComment())
						.append(", ")
						.append(confMsgLogdto.getFundParticipantId())
						.append(", ")
						.append(confMsgLogdto.getContraParticipantId())
						.append(", ")
						.append(confMsgLogdto.getContraTradeReference())
						.append(", ")
						.append(confMsgLogdto.getProductTypeId())
						.append(", ")
						.append(confMsgLogdto.getTransactionTypeId())
						.append(", ")
						.append(confMsgLogdto.getContraTradeSupplement())
						.append(") ");
			return sql.toString();
	}
	
	public Map<String, String> getSortingFields() {		
		if(CommonUtil.isNullOrEmpty(sortingFieldsMap)){
			sortingFieldsMap = new HashMap<String, String>();
			sortingFieldsMap.put("ConfirmType", "CT");
			sortingFieldsMap.put("Counterparty", "CP");
			sortingFieldsMap.put("Folder", "FO");
			sortingFieldsMap.put("Fund", "F");
			sortingFieldsMap.put("Product", "TT");
			sortingFieldsMap.put("CaptureAge", "A");
			sortingFieldsMap.put("ConfirmStatus", "CS");
		}
		return sortingFieldsMap;
	}
	
	public String getClearingFacilityIds(String clearingFacilityIdJson) {
		if(!CommonUtil.isNullOrEmpty(clearingFacilityIdJson) && (clearingFacilityIdJson.equalsIgnoreCase("999") || clearingFacilityIdJson.equalsIgnoreCase("8"))){
			String clearingIds = "";
			for (String ids : StaticValuesUtil.getClearingFacilityList(clearingFacilityIdJson)) {
				clearingIds = ids + "," + clearingIds; 
			}
			return clearingIds.substring(0, clearingIds.length()-1);
		}
		return clearingFacilityIdJson;
	}
	
	public String findTradeTypeFlag(String code){
		TradeTypeDTOFilter tradeTypeDTOFilter = new TradeTypeDTOFilter();
		tradeTypeDTOFilter.setCode(Arrays.asList(code));
		
		List<TradeTypeDTO> tradeTypeList = findTradeTypeByFilter(tradeTypeDTOFilter);
		if(!CommonUtil.isNullOrEmpty(tradeTypeList) && tradeTypeList.size()>0){
			TradeTypeDTO dto = (TradeTypeDTO) tradeTypeList.get(0);
			if(dto.getTradeTypeId() != 0 && dto.getOtcFlag()){
				return "OTC";
			}else{
				return "NON-OTC";
			}
		}		
		return null;
	}
	
	public Short getConfirmType(String tradeEventTypeId, boolean isPartial){
		String tradeEventType = null;
		Short dashBoardItemTypeId = null;
		
		if (tradeEventTypeId.equalsIgnoreCase(DashBoardItemType.TERMINATION) && isPartial){
			tradeEventType = DashBoardItemType.PARTIAL_TERMINATION;
		}else if (tradeEventTypeId.equalsIgnoreCase(DashBoardItemType.ASSIGNMENT) && isPartial){
			tradeEventType = DashBoardItemType.PARTIAL_ASSIGNMENT;
		}else{
			tradeEventType = tradeEventTypeId;
		}
		
		switch (tradeEventType) {
		case DashBoardItemType.ORIGINAL:
			dashBoardItemTypeId = DashBoardItemType.ORIGINAL_ID;                        
			break;

		case DashBoardItemType.TERMINATION:
			dashBoardItemTypeId = DashBoardItemType.TERMINATION_ID;                                             
			break;

		case DashBoardItemType.ASSIGNMENT:
			dashBoardItemTypeId = DashBoardItemType.ASSIGNMENT_ID;                                              
			break;

		case DashBoardItemType.PARTIAL_ASSIGNMENT:
			dashBoardItemTypeId = DashBoardItemType.PARTIAL_ASSIGNMENT_ID;                                      
			break;

		case DashBoardItemType.PARTIAL_TERMINATION:
			dashBoardItemTypeId = DashBoardItemType.PARTIAL_TERMINATION_ID;                                    
			break;

		case DashBoardItemType.EXERCISE:
			dashBoardItemTypeId = DashBoardItemType.EXERCISE_ID;                                      
			break;

		default:
			break;
		}

		return dashBoardItemTypeId;
	}
	
	/**
	 * @return the ldapAuthenticationService
	 */
	public LdapAuthenticationService getLdapAuthenticationService() {
		return ldapAuthenticationService;
	}

	/**
	 * @param ldapAuthenticationService the ldapAuthenticationService to set
	 */
	public void setLdapAuthenticationService(LdapAuthenticationService ldapAuthenticationService) {
		this.ldapAuthenticationService = ldapAuthenticationService;
	}

	/**
	 * @return the savedSearchmap
	 */
	public Map<String, String> getSavedSearchmap() {
		return savedSearchmap;
	}

	/**
	 * @param savedSearchmap the savedSearchmap to set
	 */
	public void setSavedSearchmap(Map<String, String> savedSearchmap) {
		this.savedSearchmap = savedSearchmap;
	}

	/**
	 * @return the referenceDataMap
	 */
	public Map<String, Integer> getReferenceDataMap() {
		return referenceDataMap;
	}

	/**
	 * @param referenceDataMap the referenceDataMap to set
	 */
	public void setReferenceDataMap(Map<String, Integer> referenceDataMap) {
		this.referenceDataMap = referenceDataMap;
	}

	/**
	 * @return the commonSQLExecuterDAO
	 */
	public CommonSQLExecuterDAO getCommonSQLExecuterDAO() {
		return commonSQLExecuterDAO;
	}

	/**
	 * @param commonSQLExecuterDAO the commonSQLExecuterDAO to set
	 */
	public void setCommonSQLExecuterDAO(CommonSQLExecuterDAO commonSQLExecuterDAO) {
		this.commonSQLExecuterDAO = commonSQLExecuterDAO;
	}

	/**
	 * @return the confObjectQueryDAO
	 */
	public ConfObjectQueryDAO getConfObjectQueryDAO() {
		return confObjectQueryDAO;
	}

	/**
	 * @param confObjectQueryDAO the confObjectQueryDAO to set
	 */
	public void setConfObjectQueryDAO(ConfObjectQueryDAO confObjectQueryDAO) {
		this.confObjectQueryDAO = confObjectQueryDAO;
	}

	public List<DashBoardItemFormBean> loadDashBoardItemDTOs(String gtid, String clientId, String tradeEventId) {
		boolean isExistingTrade = false;
		List<DashBoardItemFormBean> dashboardDTOs=null;
		String sql=
				"select te.TradeEventId, te.PartialFlag, th.TradeId, th.FundId, th.CounterpartyId, th.PrimeBrokerAccountId,te.EventDate,th.LastModifiedDateTime "+
				"from TradeHeader th "+
				"join TradeEvent te on (th.TradeId = te.TradeId) "+
				"join Fund f on (th.FundId = f.FundId) "+
				"where f.ClientId =  "+clientId+
				"and te.CaptureSystemTradeId = '"+gtid+"' "+
				"and te.TradeEventTypeId = '"+tradeEventId+"' ";
		
		log.info("loadDashBoardItemDTOs : Query : " + sql);
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sql);
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			dashboardDTOs  =new ArrayList<DashBoardItemFormBean>();
			for(Object[] resultObj : listDTO.getObjectArrayList()){
				//Object[] resultObj = (Object[]) result;
				String existingReocrdSQL=" select dbi.DashBoardItemId, dbis.Description as ConfirmStatus,dbis.ShortName " +
						" from DashBoardItem dbi " +
						" join DashBoardItemStatus dbis on (dbi.DashBoardItemStatusId = dbis.DashBoardItemStatusId) " +
						" where TradeEventId="+String.valueOf(resultObj[0])+" and DashBoardSessionId in (1,2,12)";
				
				log.info("loadDashBoardItemDTOs : ExistingRecordSQL : " + existingReocrdSQL);
				//List<Object> dbResultList = CommonSQLExecuterDAO.getInstance().executeSQL(existingReocrdSQL);
				ListDTO listDTO2 = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sql, ListDTO.class);
				if(!CommonUtil.isNull(listDTO2) && !CommonUtil.isNullOrEmpty(listDTO2.getObjectArrayList())){
					isExistingTrade=true;
				}
				if(CommonUtil.isNullOrEmpty(listDTO2.getObjectArrayList()) && !isExistingTrade){
					com.globeop.gmt.data.dashBoardItem.Response.Results.DashBoardItemDTO dto=new com.globeop.gmt.data.dashBoardItem.Response.Results.DashBoardItemDTO();
					DashBoardItemFormBean boardItemFormBean = new DashBoardItemFormBean();
					int partialFlag = Integer.parseInt(String.valueOf(resultObj[1]));
					
					dto.setTradeEventId(String.valueOf(resultObj[0]));
					dto.setTradeId(String.valueOf(resultObj[2]));
					dto.setFundId(String.valueOf(resultObj[3]));
					dto.setCounterpartyId(String.valueOf(resultObj[4]));
					dto.setPrimeBrokerAccountId(String.valueOf(resultObj[5]));
					java.sql.Date itemDate = CommonUtil.isNull(resultObj[6]) ? null:(java.sql.Date) resultObj[6];
					Timestamp tradeLastModifiedDateTime = CommonUtil.isNull(resultObj[7]) ? null : (Timestamp)resultObj[7];
					
					if(itemDate!=null){
						dto.setItemDate(itemDate.toString());
					}
					
					if(tradeLastModifiedDateTime != null){
						dto.setTradeLastModifiedDateTime(tradeLastModifiedDateTime.toString());
					}
					
					boardItemFormBean.setTradePartial((partialFlag == 1)?true:false);
					boardItemFormBean.setDashBoardItemDTO(dto);
					dashboardDTOs.add(boardItemFormBean);
				}else{
					Object[] dbObj = (Object[])listDTO2.getObjectArrayList().get(0);
					DashBoardItemFormBean boardItemFormBean=new DashBoardItemFormBean();
					boardItemFormBean.setTradeExist(true);
					boardItemFormBean.setTradeConfirmStatus(String.valueOf(dbObj[2]));
					dashboardDTOs.add(boardItemFormBean);
				}
			}
		}
		return dashboardDTOs;
	}

	public String processUpdateTadeSettlement(ManagerialButtonFormBean formBean) {
		String responseStr = "";
		StringBuilder sb = new StringBuilder();
		String tradeSettlementIds       = formBean.getTradeIds()==null?null:String.join(",", formBean.getTradeIds());
		boolean commaCheck = false;
		sb.append(" Update TepTradeSettlement ");
		sb.append(" set ");
		if(!CommonUtil.isNullOrEmpty(formBean.getEventStatusIds())){
			commaCheck = true;
			sb.append(" SettlementStatusId= "+formBean.getEventStatusIds());
		}
		if(!CommonUtil.isNullOrEmpty(formBean.getPartialSettlement())){
			if(commaCheck){
				sb.append(" ,");
			}
			sb.append(" PartialSettlementQuantity= "+formBean.getPartialSettlement());
			commaCheck = true;
		}
		if(!CommonUtil.isNullOrEmpty(formBean.getDateOfBuyIn())){
			if(commaCheck){
				sb.append(" ,");
			}
			sb.append(" BuyInDate='"+formBean.getDateOfBuyIn()+"' ");
			commaCheck = true;
		}
		if(!CommonUtil.isNullOrEmpty(formBean.getComments())){
			if(commaCheck){
				sb.append(" ,");
			}
			sb.append(" Comments='"+formBean.getComments()+"' ");
		}
		
		sb.append(" ,LastModifiedDateTime = getDate() ");
		sb.append(" ,LastModifiedUserId   = "+formBean.getUserId());
		sb.append(" where ");
		sb.append(" TradeSettlementId in ("+tradeSettlementIds+") ");
		log.info(" query:::"+sb.toString());
		//int resultSet = CommonSQLExecuterDAO.getInstance().updateSQL(sb.toString());
		int resultSet = restTemplate.getForObject(matchingServiceURL + "/executeUpdateSQL.go?query=" + sb.toString(), Integer.class);
		if(resultSet > 0){
			responseStr = "Value updated Successfully.";
		}else{
			responseStr = "Failed to update value.";
		}
		return responseStr;
	}

	public List<Filter> getExchangeTypeList() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select ExchangeId,ShortName,Description from Exchange ");
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sb.toString());
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sb.toString(), ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			List<Filter> list = new ArrayList<Filter>(listDTO.getObjectArrayList().size());
			list.add(allFilter);

			if(!CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0;i<listDTO.getObjectArrayList().size();i++){
					Object obj[] = (Object[])listDTO.getObjectArrayList().get(i);
					Filter filter = new Filter();
					filter.setId(String.valueOf(obj[0]));
					filter.setName(String.valueOf(obj[1])+"  --  "+String.valueOf(obj[2]));
					filter.setSELECTED(false);
					list.add(filter);
				}
			}
			return list;
		}else{
			return null;
		}
	}
	
	public List<Filter> getTradeSettlementStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select SettlementStatusId,Code,ShortName from TepSettlementStatus ");
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sb.toString());
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sb.toString(), ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			List<Filter> list = new ArrayList<Filter>(listDTO.getObjectArrayList().size());
			list.add(allFilter);
			if(!CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0;i<listDTO.getObjectArrayList().size();i++){
					Object obj[] = (Object[])listDTO.getObjectArrayList().get(i);
					Filter filter = new Filter();
					filter.setId(String.valueOf(obj[0]));
					filter.setName(String.valueOf(obj[1])+"  --  "+String.valueOf(obj[2]));
					filter.setSELECTED(false);
					list.add(filter);
				}
			}
			return list;
		}else{
			return null;
		}
	}

	public List<Filter> getConfExternalSysStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select ExternalSystemId,ShortName from ExternalSystem ");
		//List<Object> resultList = CommonSQLExecuterDAO.getInstance().executeSQL(sb.toString());
		ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sb.toString(), ListDTO.class);
		if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			List<Filter> list = new ArrayList<Filter>(listDTO.getObjectArrayList().size());
			list.add(allFilter);
			if(!CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0;i<listDTO.getObjectArrayList().size();i++){
					Object obj[] = (Object[])listDTO.getObjectArrayList().get(i);
					Filter filter = new Filter();
					filter.setId(String.valueOf(obj[0]));
					filter.setName(String.valueOf(obj[1]));
					filter.setSELECTED(false);
					list.add(filter);
				}
			}
			return list;
		}else{
			return null;
		}
	}
	
	public String getPDFSignatureConfirmStatus(HttpServletRequest request, List<ClientDTO> clientList) {
		if(!CommonUtil.isNull(SessionUtil.getPdfSignatureConfirmStatusIds(request))){
			return SessionUtil.getPdfSignatureConfirmStatusIds(request);
		}else{
			String dbItemStatusId = "",dbItemStatus = "";
			for (ClientDTO clientDTO : clientList) {
				EmatchDataMapDTOFilter filter = new EmatchDataMapDTOFilter();
				filter.setEmatchMappingTypeShortName(Arrays.asList(EmatchDataMap.PDF_SIGNATURE_CONFIRM_STATUS));
				filter.setInternalValue(Arrays.asList(clientDTO.getShortName().trim()));
				List<EmatchDataMapDTO> dataMapDTOList = getEmatchDataMap(filter);

				if(!CommonUtil.isNullOrEmpty(dataMapDTOList)){
					String externalValue = dataMapDTOList.get(0).getExternalValue();
					String[] confirmStatus = externalValue.trim().split(",");

					DashBoardItemStatusDTOFilter dtoFilter = new DashBoardItemStatusDTOFilter();
					dtoFilter.setShortName(Arrays.asList(confirmStatus));
					dtoFilter.setDashBoardTypeId(Arrays.asList((short)4));
					List<DashBoardItemStatusDTO> itemStatusDTOs = getDashBoardItemStatusByFilter(dtoFilter);
					
					for (DashBoardItemStatusDTO dashBoardItemStatusDTO : itemStatusDTOs) {
						if(CommonUtil.isNullOrEmpty(dbItemStatusId) || !dbItemStatusId.contains(String.valueOf(dashBoardItemStatusDTO.getId()))){
							dbItemStatusId = dbItemStatusId + dashBoardItemStatusDTO.getId() + ",";
							dbItemStatus = dbItemStatus + dashBoardItemStatusDTO.getDescription().trim() + ",";
						}
					}
				}else{
					// If any client in client list is not have this mapping then return null;
					dbItemStatusId = null;
					dbItemStatus = null;
					break;
				}
			}
			Map<String, String> pdfSignatureConfMap = null;
			if(!CommonUtil.isNullOrEmpty(dbItemStatusId) && dbItemStatusId.endsWith(",")){
				dbItemStatusId = dbItemStatusId.substring(0, dbItemStatusId.length()-1);
				dbItemStatus = dbItemStatus.substring(0, dbItemStatus.length()-1);
				pdfSignatureConfMap = new HashMap<String, String>();
				pdfSignatureConfMap.put(dbItemStatusId, dbItemStatus);
			}
			SessionUtil.setPdfSignatureConfirmStatusMap(request, pdfSignatureConfMap);
			return dbItemStatusId;
		}
	}

	public String getDTCCTradeReferenceType(Integer fundId) {
		FundDTO fundDTO = getFundById(fundId);
		if(!CommonUtil.isNull(fundDTO)){
			return fundDTO.getDerivServTradeReferenceType().getCode();
		}
		return null;
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public Map<String,String> getMailAddressForClient(){
		Map<String,String> mailMap 	  = new HashMap<String,String>();
		EmatchDataMapDTOFilter filter = new EmatchDataMapDTOFilter();
		filter.setEmatchMappingTypeShortName(Arrays.asList("INTERNAL_EMAIL_GROUP"));
		List<EmatchDataMapDTO> ematchDataMapDTOList = gmtClientServiceManager.findEmatchDataMapByFilter(filter);
		if(!CommonUtil.isNullOrEmpty(ematchDataMapDTOList)){
			for(EmatchDataMapDTO mapDto : ematchDataMapDTOList){
				mailMap.put(mapDto.getInternalValue(), mapDto.getExternalValue());
			}
		}
		return mailMap;
	}

	/*public void findConfTradeByDBItemId(String dashBoardItemId) {
		try {
			List<Object> resultList = commonSQLExecuterDAO.executeSQL(SELECT_CONF_TRADE_MATCHED.replace("?", dashBoardItemId));
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "findConfTradeByDBItemId()", e);
		}		
	}*/
	
	public Long findMessageIdByDBItemId(String dashBoardItemId) {
		Long returnId = -1L;
		try {
			//List<Object> resultList = commonSQLExecuterDAO.executeSQL(SELECT_MESSAGEID_BY_DBITEMID.replace("?", dashBoardItemId));
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfStringByQuery.go?query=" + SELECT_MESSAGEID_BY_DBITEMID.replace("?", dashBoardItemId), ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
				for(int i=0; i<listDTO.getStringList().size(); i++){
					returnId = new Long(listDTO.getStringList().get(i));
					//trackerStatusDto.setLastModifiedDateTime(String.valueOf(rowData[5]));
				}
			}
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "findMessageIdByDBItemId()", e);
		}
		return returnId;
	}

	/**
	 * @method getTop5Matches
	 * @param string
	 * @param messageId
	 * @param isCOMClient
	 */
	public void getTop5Matches(String dashBoardItemId, Long messageId, boolean isCOMClient) {
		try {
			String[] dataTypes = new String[3];	
			dataTypes[0] = "long";
			dataTypes[1] = "long";
			dataTypes[2] = "short";
			
			Object[] paramValues = new Object[3];
			paramValues[0] = Long.valueOf(dashBoardItemId);
			paramValues[1] = messageId;
			paramValues[2] = 0;
			
			//StoredProcResults spResults = executeStoredProc(Database.SP_CONFCREDITDEFAULTSWAP, paramValues, dataTypes);
			StoredProcResults spResults = executeStoredProcReadOnly(Database.SP_CONFCREDITDEFAULTSWAP, paramValues, dataTypes);
			
			List<StoredProcResultSet> spResultSets = spResults.getAllResultSets();
			for(StoredProcResultSet spResultSet : spResultSets){
				if(spResultSet.getColumnCount()>1){
					TradeDetailsDTO tradeDetailsDTO = TradeSearchHelper.setDataToTradeDetailsDTO(spResultSet);
					List<ConfTradeCDSDTO> confTradeCDSDTOList = findConfTradeCDSByFilter(tradeDetailsDTO);
					System.out.println(tradeDetailsDTO);
				}
			}			
		} catch (Exception e) {
			CommonUtil.loggerErrorMessage(log, "getTop5Matches()", e);
		}
	}

	private List<ConfTradeCDSDTO> findConfTradeCDSByFilter(TradeDetailsDTO tradeDetailsDTO) {
		List<ConfTradeCDSDTO> returnList = null;
		try{
			StringBuffer sb = new StringBuffer();		
			sb.append(" SELECT ");
			sb.append(" * FROM ConfTradeMatched t");
			sb.append(" WHERE ");
			sb.append(" t.CounterpartyId = "+tradeDetailsDTO.getCounterpartyIdString()+" ");	
			if (!CommonUtil.isNull(tradeDetailsDTO.getFundIdString()) &&  !"-1".equals(tradeDetailsDTO.getFundIdString())){
				sb.append(" AND t.FundId = "+tradeDetailsDTO.getFundIdString()+" ");			
			}
			if (!CommonUtil.isNull(tradeDetailsDTO.getTransactionType())){
				if(tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Trade")){			
					sb.append("AND t.ExternalSystemStatusName IN ('Alleged','Unconfirmed','Confirmed','Error','YourTRIChanged','ContraTRIChanged')");
				}else{			
					sb.append("AND t.ExternalSystemStatusName IN ('Alleged','Pending-Alleged','Pending','Unconfirmed','Matched','Confirmed','Error')");
				}
			}

			sb.append(" AND t.DashBoardItemId = NULL ");		// Need to verify.			
			if (tradeDetailsDTO.getTransactionType()!=null 
					&& (tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Trade") 
							|| tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Assignment") 
							|| tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Termination") 
							|| tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Amendment"))){
				if(tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Trade") /*&& vo.getCOMClientFlag().trim().equals("trueamendment")*/){					
					sb.append(" AND t.TransactionTypeName IN ('Amendment', 'Exit', 'FeeAmendment') ");					
				}else if (tradeDetailsDTO.getTransactionType().equalsIgnoreCase("Amendment")){
					sb.append(" AND t.TransactionTypeName IN ('Amendment', 'FeeAmendment') ");
				}else{
					sb.append(" AND t.TransactionTypeName = '"+tradeDetailsDTO.getTransactionType()+"' ");
				}
			}
			sb.append("ORDER BY t.LastModifiedDateTime desc");

			//List<Object> list = commonSQLExecuterDAO.executeSQL(sb.toString());
			ListDTO listDTO = restTemplate.getForObject(matchingServiceURL + "/fetchListOfObjectArrayByQuery.go?query=" + sb.toString(), ListDTO.class);
			if(!CommonUtil.isNull(listDTO) && !CommonUtil.isNullOrEmpty(listDTO.getObjectArrayList())){
			//if(!CommonUtil.isNull(list) && list.size()>0){
				returnList = new ArrayList<ConfTradeCDSDTO>();
				for (Object[] data : listDTO.getObjectArrayList()) {
					//Object[] data = (Object[])rowData;
					ConfTradeCDSDTO vo = new ConfTradeCDSDTO();
					vo.setActivityId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.ACTIVITYID])));
					vo.setActivityName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.ACTIVITYNAME]));
					vo.setAttachmentPoint(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.ATTACHMENTPOINT])));
					vo.setBuyerPartyId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BUYERPARTYID])));
					vo.setCollateralPayerId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COLLATERALPAYERID])));
					vo.setCollateralPercentage(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COLLATERALPERCENTAGE])));
					vo.setCollateralReceiverId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COLLATERALRECEIVERID])));
					vo.setContractualSupplementId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CONTRACTUALSUPPLEMENTID])));
					vo.setContractualSupplementName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CONTRACTUALSUPPLEMENTNAME]));
					vo.setContractualTermShortName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CONTRACTUALTERMSHORTNAME]));
					vo.setContractualTermSupplementId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CONTRACTUALTERMSUPPLEMENTID])));
					vo.setCounterpartyId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COUNTERPARTYID])));
					vo.setCounterpartyParticipantId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COUNTERPARTYPARTICIPANTID])));
					vo.setCounterpartyTradeId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COUNTERPARTYTRADEID]));
					vo.setDashBoardItemId(Long.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.DASHBOARDITEMID])));
					vo.setdKReason(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.DKREASON]));
					vo.setDocumentationPublicationDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.DOCUMENTATIONPUBLICATIONDATE]));
					vo.setEffectiveDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EFFECTIVEDATE]));
					vo.setEventPaymentAmount(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTAMOUNT])));
					vo.setEventPaymentCurrencyId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTCURRENCYID]));
					vo.setExhaustionPoint(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EXHAUSTIONPOINT])));
					vo.setExitMsg(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EXITMSG]));
					vo.setExternalSystemStatusId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EXTERNALSYSTEMSTATUSID])));
					vo.setExternalSystemStatusName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EXTERNALSYSTEMSTATUSNAME]));
					vo.setExternalSystemTradeId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EXTERNALSYSTEMTRADEID]));
					vo.setFixedRate(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FIXEDRATE])));
					vo.setFundId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FUNDID])));
					vo.setFundParticipantId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FUNDPARTICIPANTID])));
					vo.setInitialPaymentAmount(new BigDecimal(CommonUtil.getStringValue(ConfTradeCDSDTOIndex.INITIALPAYMENTAMOUNT)));
					vo.setInitialPaymentCurrencyId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTCURRENCYID]));
					vo.setInitialPaymentPayerId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTPAYERID])));
					vo.setInitialPaymentReceiverId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTRECEIVERID])));
					vo.setLiquidationDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.LIQUIDATIONDATE]));
					vo.setLiquidationEffectiveDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.LIQUIDATIONEFFECTIVEDATE]));
					vo.setMasterAgreementDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERAGREEMENTDATE]));
					vo.setMasterAgreementName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERAGREEMENTNAME]));
					vo.setMasterAgreementTypeId(Integer.valueOf(CommonUtil.getStringValue(ConfTradeCDSDTOIndex.MASTERAGREEMENTTYPEID)));
					vo.setMasterConfirmationAnnexDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERCONFIRMATIONANNEXDATE]));
					vo.setMasterConfirmationDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERCONFIRMATIONDATE]));
					vo.setMasterConfirmationTypeId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERCONFIRMATIONTYPEID])));
					vo.setMasterConfirmName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MASTERCONFIRMNAME]));
					vo.setMatrixTermId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MATRIXTERMID])));
					vo.setMatrixTermName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MATRIXTERMNAME]));
					vo.setMatrixTypeId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MATRIXTYPEID])));
					vo.setMatrixTypeName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MATRIXTYPENAME]));
					vo.setMaturityDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MATURITYDATE]));
					vo.setNotionalAmount(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.NOTIONALAMOUNT])));
					vo.setNotionalCurrencyId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.NOTIONALCURRENCYID]));
					vo.setPeriodicPaymentFirstPayDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PERIODICPAYMENTFIRSTPAYDATE]));
					vo.setPeriodicPaymentMultiplier(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PERIODICPAYMENTMULTIPLIER]));
					vo.setPeriodicPaymentPeriod(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PERIODICPAYMENTPERIOD]));
					vo.setPeriodicPaymentRollConv(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PERIODICPAYMENTROLLCONV]));
					vo.setProductTypeId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PRODUCTTYPEID])));
					vo.setProductTypeName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.PRODUCTTYPENAME]));
					vo.setRemainingNotional(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REMAININGNOTIONAL])));
					vo.setRemainingNotionalCurrencyId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REMAININGNOTIONALCURRENCYID]));
					vo.setRemainingPartyId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REMAININGPARTYID])));
					vo.setSellerPartyId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SELLERPARTYID])));
					vo.setSettledEntityMatrixPubDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SETTLEDENTITYMATRIXPUBDATE]));
					vo.setSettledEntityMatrixSource(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SETTLEDENTITYMATRIXSOURCE]));
					vo.setTrackerStatusId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRACKERSTATUSID])));
					vo.setTradeDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRADEDATE]));
					vo.setTradeId(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRADEID])));
					vo.setTradeSupplementId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRADESUPPLEMENTID]));
					vo.setTransactionTypeId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSACTIONTYPEID])));
					vo.setTransactionTypeName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSACTIONTYPENAME]));
					vo.setTransfereeId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSFEREEID])));
					vo.setTransferorId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSFERORID])));
					vo.setUnderlyingAnnexDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.UNDERLYINGANNEXDATE]));
					vo.setUnderlyingId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.UNDERLYINGID]));
					vo.setUnderlyingName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.UNDERLYINGNAME]));
					vo.setRestructuringEvent(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.RESTRUCTURINGEVENT]));
					vo.setModifiedEquityDelivery(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MODIFIEDEQUITYDELIVERY]));
					vo.setTransfereeShortName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSFEREESHORTNAME]));
					vo.setRemainingPartyShortName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REMAININGPARTYSHORTNAME]));
					vo.setEventPaymentPayerId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTPAYERID])));
					vo.setEventPaymentPayer(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTPAYER]));
					vo.setEventPaymentReceiverId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTRECEIVERID])));
					vo.setEventPaymentReceiver(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTRECEIVER]));
					vo.setTrackerStatus(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRACKERSTATUS]));
					vo.setInitialPaymentPayer(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTPAYER]));
					vo.setInitialPaymentReceiver(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTRECEIVER]));
					vo.setFundParticipant(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FUNDPARTICIPANT]));
					vo.setCounterpartyParticipant(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COUNTERPARTYPARTICIPANT]));
					vo.setFundShortName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FUNDSHORTNAME]));
					vo.setCounterpartyShortName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COUNTERPARTYSHORTNAME]));
					vo.setBusinessCenterId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BUSINESSCENTERID])));
					vo.setBusinessCenterName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BUSINESSCENTERNAME]));
					vo.setEventPaymentAmountString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTAMOUNT]));
					vo.setCollateralPayerName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COLLATERALPAYERNAME]));
					vo.setCollateralReceiverName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.COLLATERALRECEIVERNAME]));
					vo.setTransfereeName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSFEREENAME]));
					vo.setTransferorName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.TRANSFERORNAME]));
					vo.setRemainingPartyName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REMAININGPARTYNAME]));
					vo.setSinglePaymentAmountString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SINGLEPAYMENTAMOUNT]));
					vo.setSinglePaymentDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SINGLEPAYMENTDATE]));
					vo.setSinglePaymentCurrencyId(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SINGLEPAYMENTCURRENCYID]));
					vo.setEventPaymentDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.EVENTPAYMENTDATE]));
					vo.setBuyerPartyParticipant(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BUYERPARTYPARTICIPANT]));
					vo.setSellerPartyParticipant(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SELLERPARTYPARTICIPANT]));
					vo.setInitialPaymentDateString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INITIALPAYMENTDATE]));
					vo.setFullFirstCalculationPeriod(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FULLFIRSTCALCULATIONPERIOD]));
					vo.setLien(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.LIEN]));
					vo.setSecuredList(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SECUREDLISTAPPLICABILITY]));
					vo.setFacilityType(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FACILITYTYPE]));
					vo.setCreditAgreementDateAsString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CREDITAGREEMENTDATE]));
					vo.setBorrower(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BORROWER]));
					vo.setInsurer(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INSURER]));
					vo.setSector(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.SECTOR]));
					vo.setReferencePolicy(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REFERENCEPOLICY]));
					vo.setMortgageMaturityAsString(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MORTGAGEMATURITY]));
					vo.setMortgagePaymentPeriod(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MORTGAGEPAYMENTPERIOD]));
					vo.setMortgagePaymentMultiplier(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MORTGAGEPAYMENTMULTIPLIER])));
					vo.setMortgageOriginalPrincipalAmount(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.MORTGAGEORIGINALPRINCIPALAMT])));
					vo.setRateSourceId(Integer.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.RATESOURCEID])));
					vo.setReferencePrice(new BigDecimal(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.REFERENCEPRICE])));
					vo.setRateSourceName(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.RATESOURCENAME]));
					vo.setInterestShortFallCap(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INTERESTSHORTFALLCAP]));
					vo.setInterestShortFallCompounding(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INTERESTSHORTFALLCOMPOUNDING]));
					vo.setInterestShortFallCapBasis(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.INTERESTSHORTFALLCAPBASIS]));
					vo.setOptionalEarlyTermination(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.OPTIONALEARLYTERMINATION]));
					vo.setFixedAmountPaymentDelay(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.FIXEDAMOUNTPAYMENTDELAY]));
					vo.setwACCapInterestProvision(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.WACCAPINTERESTPROVISION]));
					vo.setStepUpProvision(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.STEPUPPROVISION]));
					vo.setBusinessDayConvention(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.BUSINESSDAYCONVENTION]));
					vo.setClientTradeFlag(Short.valueOf(CommonUtil.getStringValue(data[ConfTradeCDSDTOIndex.CLIENTTRADEFLAG])));	
					
					returnList.add(vo);
				}
			}


		} catch(Exception e){
			CommonUtil.loggerErrorMessage(log, "findConfTradeCDSByFilter", e);
		}
		return returnList;
	}
	
	/* Read-only Stored proc API	 */
	public StoredProcResults executeStoredProcReadOnly(String sp, Object[] procParam, String[] procType) throws Exception{
		return clientServiceManagerReadOnly.executeStoredProc(sp, procParam, procType);
	}
	
	/* Read/Write API	 */
	
	public StoredProcResults executeStoredProc(String sp, Object[] procParam, String[] procType) throws Exception{
		return clientServiceManager.executeStoredProc(sp, procParam, procType);
	}
	
	public TradeAliasDTO persistTradeAlias(TradeAliasDTO tradeAliasDTO) {
		return clientServiceManager.persistTradeAlias(tradeAliasDTO);
	}
	
	public DashBoardItemDTO persistDashBoardItem(DashBoardItemDTO dto){
        return clientServiceManager.persistDashBoardItem(dto);
    }
	
	public List<EntitlementUserRoleDTO> findEntitlementUserRoleByFilter(EntitlementUserRoleDTOFilter dtoFilter){
		return clientServiceManager.findEntitlementUserRoleByFilter(dtoFilter);
	}
	
	public  EntitlementUserRoleDTO findEntitlementUserRoleById(Integer id){
		return clientServiceManager.findEntitlementUserRoleById(id);
	}
	
	@Cacheable(key="#root.methodName", value=Framework.COMMON_CACHE)
	public List<EntitlementRoleDTO> getEntitlementRole(){
		EntitlementRoleDTOFilter dtoFilter = new EntitlementRoleDTOFilter();
		dtoFilter.setEntitlementTypeId(Arrays.asList(Short.valueOf("8"),Short.valueOf("6")));
		return clientServiceManager.findEntitlementRoleByFilter(dtoFilter);
	}
	
	public List<Filter> getEntitlementRoleList(){
		List<Filter> returnList = new ArrayList<Filter>();
		try{
			List<EntitlementRoleDTO> entitlementRoleDTOs = getEntitlementRole();
			for (EntitlementRoleDTO roleDTO : entitlementRoleDTOs) {
				Filter ft = new Filter();
				ft.setId(roleDTO.getEntitlementRoleId().toString());
				ft.setName(roleDTO.getShortName());
				ft.setSELECTED(false);
				returnList.add(ft);
			}
			return returnList;
		} catch (Exception e){
			CommonUtil.loggerErrorMessage(log, "getEntitlementRoleList", e);
		}
		return null;
	}

	/**
	 * @return the clientServiceManagerReadOnly
	 */
	public ClientServiceManager getClientServiceManagerReadOnly() {
		return clientServiceManagerReadOnly;
	}

	/**
	 * @param clientServiceManagerReadOnly the clientServiceManagerReadOnly to set
	 */
	public void setClientServiceManagerReadOnly(ClientServiceManager clientServiceManagerReadOnly) {
		this.clientServiceManagerReadOnly = clientServiceManagerReadOnly;
	}
}
