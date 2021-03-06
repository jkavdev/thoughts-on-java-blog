/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thoughts.on.java.part1.find;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thoughts.on.java.JpaJunitConfig;
import org.thoughts.on.java.model.Author;

/**
 * @author Thorben
 */
public class TestStatistics extends JpaJunitConfig {

    @Test
    public void selectAuthors() {
        List<Author> authors = getEntityManager().createQuery("SELECT a FROM Author a",
                Author.class).getResultList();

        authors.forEach(a -> System.out.println("Author "
                + a.getFirstName()
                + " "
                + a.getLastName()
                + " wrote "
                + a.getBooks().size()
                + " books."));
    }
}
