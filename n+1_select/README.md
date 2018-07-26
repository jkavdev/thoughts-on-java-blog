# How to find and fix n+1 Select issues with Hibernate

# Part1 - What is the n+1 select issue and how to find it

* Teste que gera o n+1

        @Test
            public void selectAuthors() {
                List<Author> authors = em.createQuery("SELECT a FROM Author a",
                        Author.class).getResultList();
                for (Author a : authors) {
                    System.out.println("Author "
                            + a.getFirstName()
                            + " "
                            + a.getLastName()
                            + " wrote "
                            + a.getBooks().size()
                            + " books.");
                }
            }

* Habilitando as estatisticas do Hibernate no `persistence.xml`

        <property name="hibernate.generate_statistics" value="true"/>
        
* Resultado 
* Informando o tempo e a quantidade de registros retornados `time: 27ms, rows: 11`

        22:18:26,109 DEBUG ConcurrentStatisticsImpl:412 - HHH000117: HQL: SELECT a FROM Author a, time: 27ms, rows: 11
        
* exibindo operacoes do jdbc        

        22:18:26,142  INFO StatisticalLoggingSessionEventListener:275 - Session Metrics {
            13654 nanoseconds spent acquiring 1 JDBC connections;
            0 nanoseconds spent releasing 0 JDBC connections;
            3537926 nanoseconds spent preparing 12 JDBC statements;
            1471145 nanoseconds spent executing 12 JDBC statements;
            0 nanoseconds spent executing 0 JDBC batches;
            0 nanoseconds spent performing 0 L2C puts;
            0 nanoseconds spent performing 0 L2C hits;
            0 nanoseconds spent performing 0 L2C misses;
            8700596 nanoseconds spent executing 1 flushes (flushing a total of 17 entities and 23 collections);
            54329 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
        }        

* Habilitando logs das consultas do hibernate

        log4j.logger.org.hibernate.SQL=debug
        
# Part2 - Solving n+1 select issues with `@NamedEntityGraph`       

* definindo um mapeamento para a consulta, definindo o que trazer durante a consulta com `@NamedEntityGraph`
* `name = "graph.AuthorBooks"` definimos um nome especifico para a consulta
* `attributeNodes = @NamedAttributeNode("books")` definimos o nome do atributo que queremos adicionar a consulta
* no caso estamos indicando ao trazer os autores, tambem traga os livros

        @NamedEntityGraph(name = "graph.AuthorBooks",
                attributeNodes = @NamedAttributeNode("books")) 
                    
* criando teste para a consulta
* obtemos o objeto que executa a consulta com grafo `EntityGraph<?> graph = em.createEntityGraph("graph.AuthorBooks");`
* realizamos a consulta passando o objeto com o grafo `em.createQuery("...", Author.class).setHint("javax.persistence.loadgraph", graph).getResultList();`
* estamos indicando a implementacao do objeto de grafo `.setHint("javax.persistence.loadgraph", graph)`

        @Test
            public void selectAuthors_GraphBooks() {
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
            }  
            
* adicionando um atributo do atributo no grafo
* no do atributo a ser adicionado na consulta com o grafo `attributeNodes = @NamedAttributeNode(value = "books", subgraph = "books"),`
* adicionando o atributo do atributo `subgraphs = @NamedSubgraph(name = "books", attributeNodes = @NamedAttributeNode("reviews")))})`

        @NamedEntityGraph(name = "graph.AuthorBooksReviews",
                        attributeNodes = @NamedAttributeNode(value = "books", subgraph = "books"),
                        subgraphs = @NamedSubgraph(name = "books",
                                attributeNodes = @NamedAttributeNode("reviews")))})                              

* no caso estamos indicando para trazer os livros, junto com todos os reviews do livro

* realizando teste

        @Test
            public void selectAuthors_GraphBooksAndReviews() {
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
            }        
            
# Part3 - Solving n+1 select issues with dynamic `EntityGraphs`  

* adicionando grafo dinamicamente com entityGraphs
* adicionando depedencia que gera os metadados das entidades

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-jpamodelgen</artifactId>
            <version>4.3.11.Final</version>
        </dependency>
        
* indicando ao maven para gerar os metadados

        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.2</version>
            <configuration>
                <compilerArguments>
                    <processor>org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor</processor>
                </compilerArguments>
            </configuration>
        </plugin>        
        
* as classes geradas estarao no target junto das entidades  

        Author.class
        Author_.class
        Book.class
        Book_.class
        Rating.class
        Review.class
        Review_.class        
        
* realizando a consulta com grafos dinamicos
* criando um grafo da entidade autor `em.createEntityGraph(Author.class);`
* adicionando um subgrafo dinamicamente `graph.addSubgraph(Author_.books);` atraves do metadato que indica o atributo da entidade
* adicionando um subgrafo do subgrafo dinamicamente `bookSubGraph.addSubgraph(Book_.reviews);` atraves do metadato que indica o atributo da subentidade

        @Test
            public void selectAuthors_GraphBooksAndReviews() {
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
            }        