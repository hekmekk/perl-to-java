package com.sippy.wrapper.parent;

import com.sippy.wrapper.parent.database.DatabaseConnection;
import com.sippy.wrapper.parent.database.dao.TnbDao;
import com.sippy.wrapper.parent.request.JavaTestRequest;
import com.sippy.wrapper.parent.response.JavaTestResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class WrappedMethods {

  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedMethods.class);

  @EJB DatabaseConnection databaseConnection;

  @PersistenceContext(unitName = "CustomDB")
  private EntityManager entityManager;

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

  private record GetTnbListRequest(String number) {}

  private record GetTnbListResponse(String faultCode, String faultString, List<Tnb> tnbs) {}

  private record Tnb(String tnb, String name, boolean isTnb) {}

  @RpcMethod(name = "getTnbList", description = "perl to java translation")
  public GetTnbListResponse getTnbList(final GetTnbListRequest req) {

    LOGGER.info("Fetching TNB list from the database");

    final List<TnbDao> tnbsFromDb =
        entityManager.createNativeQuery("SELECT * FROM tnbs", TnbDao.class).getResultList();

    String tnb = null;
    if (req.number() != null) {
      final var oneTnbQuery =
          entityManager.createNativeQuery("SELECT * FROM tnbs WHERE tnb = ?", TnbDao.class);
      oneTnbQuery.setParameter(1, req.number());

      tnb =
          ((List<TnbDao>) oneTnbQuery.getResultList())
              .stream().findFirst().map(TnbDao::getTnb).orElse(null);
    }

    var tnbs = new ArrayList<Tnb>();
    tnbs.add(new Tnb("D001", "Deutsche Telekom", "D001".equals(tnb)));
    for (TnbDao tnb_from_db : tnbsFromDb) {
      if (List.of("D146", "D218", "D248").contains(tnb_from_db.getTnb())) {
        continue;
      }

      tnbs.add(
          new Tnb(tnb_from_db.getTnb(), tnb_from_db.getName(), tnb_from_db.getTnb().equals(tnb)));
    }

    tnbs.sort((a, b) -> a.name().toLowerCase().compareTo(b.name().toLowerCase()));

    return new GetTnbListResponse("200", "Method success", tnbs);
  }
}
