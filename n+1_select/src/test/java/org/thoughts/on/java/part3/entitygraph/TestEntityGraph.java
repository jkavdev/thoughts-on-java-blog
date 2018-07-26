/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thoughts.on.java.part3.entitygraph;

import org.junit.Test;
import org.thoughts.on.java.JpaJunitConfig;
import org.thoughts.on.java.model.Author;
import org.thoughts.on.java.model.Author_;
import org.thoughts.on.java.model.Book;
import org.thoughts.on.java.model.Book_;

import javax.persistence.EntityGraph;
import javax.persistence.Subgraph;
import java.util.List;
import java.util.stream.Collectors;

public class TestEntityGraph extends JpaJunitConfig {

    @Test
    public void selectAuthors_GraphBooksAndReviews() {
        EntityGraph graph = getEntityManager().createEntityGraph(Author.class);
        Subgraph<Book> bookSubGraph = graph.addSubgraph(Author_.books);
        bookSubGraph.addSubgraph(Book_.reviews);

        List<Author> authors = getEntityManager().createQuery("SELECT DISTINCT a FROM Author a",
                Author.class)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();

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
