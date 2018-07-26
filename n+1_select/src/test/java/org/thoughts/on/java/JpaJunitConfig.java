package org.thoughts.on.java;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaJunitConfig {

    private static EntityManagerFactory emf;

    @Before
    public void init() {
        emf = Persistence.createEntityManagerFactory("my-persistence-unit");
    }

    @After
    public void closeEntityManager() {
        System.out.println("after");
        getEntityManager().close();
    }

    @AfterClass
    public static void closeEntityFactory() {
        System.out.println("afterclass");
        emf.close();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
