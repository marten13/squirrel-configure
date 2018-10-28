package nl.berg.squirrel_configure_xml.util;

import nl.berg.squirrel_configure_xml.domain.DriverConfig;
import nl.berg.squirrel_configure_xml.domain.predicate.HeeftChildNodeMetNaamEnWaardePredicate;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

import static nl.berg.squirrel_configure_xml.util.TestUtil.createNewDocument;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.*;

public class DriverUtilTest {

    String jdbcUrl = "jdbc:as400://172.30.253.31";
    String db2Lib = "D:\\db2Lib\\jt400.jar";
    String driverName = "TestDriver";

    @Test
    public void testAddNewDriver() {
        Document document = createNewDocument();
        String testDriverUUID = DriverUtil.addNewDriver(document, new DriverConfig(db2Lib, driverName, null));

        assertThat(testDriverUUID, not(isEmptyOrNullString()));
        Element driverByUUID = DriverUtil.findDriverBijUUID(document, testDriverUUID);
        assertThat(driverByUUID, not(nullValue()));

        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("driverClassName", "com.ibm.as400.access.AS400JDBCDriver").apply(driverByUUID));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("name", driverName).apply(driverByUUID));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("url", jdbcUrl).apply(driverByUUID));

        File file = new File("target/driverTest.xml");
        DocumentUtil.writeDocument(document, file);
    }


}