/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thoughts.on.java.part2.entitygraph;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.thoughts.on.java.model.Author;

/**
 * @author Thorben
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestNamedEntityGraph {

    private EntityManagerFactory emf;

    @Before
    public void init() {
        emf = Persistence.createEntityManagerFactory("my-persistence-unit");
    }

    @After
    public void close() {
        emf.close();
    }

    @Test
    public void selectAuthors() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        List<Author> authors = em.createQuery("SELECT a FROM Author a",
                Author.class).getResultList();

        for (Author a : authors) {
            System.out.println("Author "
                    + a.getFirstName()
                    + " "
                    + a.getLastName()
                    + " wrote "
                    + a.getBooks().stream().map(b -> b.getTitle())
                    .collect(Collectors.joining(", ")));
        }

        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void selectAuthors_GraphBooks() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        EntityGraph<?> graph = em.createEntityGraph("graph.AuthorBooks");
        List<Author> authors = em.createQuery("SELECT DISTINCT a FROM Author a",
                Author.class).setHint("javax.persistence.loadgraph", graph).getResultList();

        for (Author a : authors) {
            System.out.println("Author "
                    + a.getFirstName()
                    + " "
                    + a.getLastName()
                    + " wrote "
                    + a.getBooks().stream().map(b -> b.getTitle())
                    .collect(Collectors.joining(", ")));
        }

        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void selectAuthors_GraphBooksAndReviews() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        EntityGraph<?> graph = em.createEntityGraph("graph.AuthorBooksReviews");
        List<Author> authors = em.createQuery("SELECT DISTINCT a FROM Author a",
                Author.class).setHint("javax.persistence.loadgraph", graph).getResultList();

        for (Author a : authors) {
            System.out.println("Author "
                    + a.getFirstName()
                    + " "
                    + a.getLastName()
                    + " wrote "
                    + a.getBooks()
                    .stream()
                    .map(b -> b.getTitle() + "("
                            + b.getReviews().size() + " reviews)")
                    .collect(Collectors.joining(", ")));
        }

        em.getTransaction().commit();
        em.close();
    }
}
