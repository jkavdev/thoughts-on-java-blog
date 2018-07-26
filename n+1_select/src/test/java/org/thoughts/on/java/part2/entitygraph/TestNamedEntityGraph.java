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
import org.thoughts.on.java.JpaJunitConfig;
import org.thoughts.on.java.model.Author;

/**
 * @author Thorben
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestNamedEntityGraph extends JpaJunitConfig {

    @Test
    public void selectAuthors() {
        List<Author> authors = getEntityManager().createQuery("SELECT a FROM Author a",
                Author.class).getResultList();

        authors.forEach(a -> System.out.println("Author "
                + a.getFirstName()
                + " "
                + a.getLastName()
                + " wrote "
                + a.getBooks().stream().map(b -> b.getTitle())
                .collect(Collectors.joining(", "))));
    }

    @Test
    public void selectAuthors_GraphBooks() {
        EntityGraph<?> graph = getEntityManager().createEntityGraph("graph.AuthorBooks");
        List<Author> authors = getEntityManager().createQuery("SELECT DISTINCT a FROM Author a",
                Author.class).setHint("javax.persistence.loadgraph", graph).getResultList();

        authors.forEach(a -> System.out.println("Author "
                + a.getFirstName()
                + " "
                + a.getLastName()
                + " wrote "
                + a.getBooks().stream().map(b -> b.getTitle())
                .collect(Collectors.joining(", "))));
    }

    @Test
    public void selectAuthors_GraphBooksAndReviews() {
        EntityGraph<?> graph = getEntityManager().createEntityGraph("graph.AuthorBooksReviews");
        List<Author> authors = getEntityManager().createQuery("SELECT DISTINCT a FROM Author a",
                Author.class).setHint("javax.persistence.loadgraph", graph).getResultList();

        authors.forEach(a -> System.out.println("Author "
                + a.getFirstName()
                + " "
                + a.getLastName()
                + " wrote "
                + a.getBooks()
                .stream()
                .map(b -> b.getTitle() + "(" + b.getReviews().size() + " reviews)")
                .collect(Collectors.joining(", "))));
    }
}
