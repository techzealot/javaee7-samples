package org.javaee7.jpa.datasourcedefinition_applicationxml_pu;

import static org.jboss.shrinkwrap.api.ArchivePaths.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.asset.EmptyAsset.INSTANCE;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.enterprise.inject.spi.CDI;

import org.javaee7.jpa.datasourcedefinition_applicationxml_pu.entity.TestEntity;
import org.javaee7.jpa.datasourcedefinition_applicationxml_pu.service.TestService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This tests that a data source defined via the data-source element in an EAR's application.xml can be used by JPA.
 * <p>
 * In this test the persistence unit is defined inside an EJB module (.jar)
 *
 * <p>
 * The actual JPA code being run is not specifically relevant; any kind of JPA operation that
 * uses the data source is okay here.
 *
 * @author Arjan Tijms
 */
@RunWith(Arquillian.class)
public class DataSourceDefinitionApplicationXMLPuEJBTest {

    @Deployment
    public static Archive<?> deploy() {
        return
        // EAR archive
        create(EnterpriseArchive.class, "testEAR.ear")

            // Data-source is defined here
            .setApplicationXML("application-ejb.xml")

            // JDBC driver for data source
            .addAsLibraries(Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("com.h2database:h2")
                .withoutTransitivity()
                .asSingleFile())

            // EJB module
            .addAsModule(
                create(JavaArchive.class, "testEJB.jar")

                    // Persistence unit is defined here, references data source
                    .addAsResource("META-INF/persistence.xml")

                    // Service class that uses persistence unit
                    .addClasses(TestEntity.class, TestService.class)
            )

            // Web module
            // This is needed to prevent Arquillian generating an illegal application.xml
            .addAsModule(
                create(WebArchive.class, "test.war")
                    // This class containing the test
                    .addClass(DataSourceDefinitionApplicationXMLPuEJBTest.class)
                    .addAsWebInfResource(INSTANCE, create("beans.xml"))

            );
    }

    @Test
    public void insertAndQueryEntity() throws Exception {

        TestService testService = CDI.current().select(TestService.class).get();

        testService.saveNewEntity();

        List<TestEntity> testEntities = testService.getAllEntities();

        assertTrue(testEntities.size() == 1);
        assertTrue(testEntities.get(0).getValue().equals("mytest"));
    }

}
