package com.sippy.wrapper.parent;

import com.sippy.wrapper.parent.database.DatabaseConnection;
import com.sippy.wrapper.parent.database.dao.TnbDao;
import com.sippy.wrapper.parent.request.GetTnbListRequest;
import com.sippy.wrapper.parent.request.JavaTestRequest;
import com.sippy.wrapper.parent.response.JavaTestResponse;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class WrappedMethods {

  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedMethods.class);

  @EJB DatabaseConnection databaseConnection;

  @RpcMethod(name = "javaTest", description = "Check if everything works :)")
  public Map<String, Object> javaTest(JavaTestRequest request) {
    JavaTestResponse response = new JavaTestResponse();

    int count = databaseConnection.getAllTnbs().size();

    LOGGER.info("the count is: " + count);

    response.setId(request.getId());
    String tempFeeling = request.isTemperatureOver20Degree() ? "warm" : "cold";
    response.setOutput(
        String.format(
            "%s has a rather %s day. And he has %d tnbs", request.getName(), tempFeeling, count));

    Map<String, Object> jsonResponse = new HashMap<>();
    jsonResponse.put("faultCode", "200");
    jsonResponse.put("faultString", "Method success");
    jsonResponse.put("something", response);

    return jsonResponse;
  }

  record RpcResponseTnb(String tnb, String name, boolean isTnb) {}

  @RpcMethod(name = "getTnbList", description = "perl to java translation")
  public Map<String, Object> getTnbList(final GetTnbListRequest req) {

    LOGGER.info("Fetching TNB list from the database");

    var tnbsFromDb = databaseConnection.getAllTnbs();

    String tnb = null;
    if (req.getNumber() != null) {
      tnb = databaseConnection.getOneTnb(req.getNumber());
    }

    var tnbs = new ArrayList<RpcResponseTnb>();
    tnbs.add(new RpcResponseTnb("D001", "Deutsche Telekom", "D001".equals(tnb)));
    for (TnbDao tnb_from_db : tnbsFromDb) {
      if (List.of("D146", "D218", "D248").contains(tnb_from_db.getTnb())) {
        continue;
      }

      tnbs.add(
          new RpcResponseTnb(
              tnb_from_db.getTnb(), tnb_from_db.getName(), tnb_from_db.getTnb().equals(tnb)));
    }

    tnbs.sort(Comparator.comparing(tnbObj -> tnbObj.name().toLowerCase()));

    Map<String, Object> jsonResponse = new HashMap<>();
    jsonResponse.put("faultCode", "200");
    jsonResponse.put("faultString", "Method success");
    jsonResponse.put("tnbs", tnbs);

    return jsonResponse;
  }
}
