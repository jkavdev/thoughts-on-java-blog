/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thoughts.on.java.part3.entitygraph;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Subgraph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thoughts.on.java.model.Author;
import org.thoughts.on.java.model.Author_;
import org.thoughts.on.java.model.Book;
import org.thoughts.on.java.model.Book_;

public class TestEntityGraph {

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
    public void selectAuthors_GraphBooksAndReviews() {
        EntityManager em = emf.createEntityManager();

        EntityGraph graph = em.createEntityGraph(Author.class);
        Subgraph<Book> bookSubGraph = graph.addSubgraph(Author_.books);
        bookSubGraph.addSubgraph(Book_.reviews);

        List<Author> authors = em.createQuery("SELECT DISTINCT a FROM Author a",
                Author.class)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();

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

        em.close();
    }
}
