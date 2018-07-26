# How to find and fix n+1 Select issues with Hibernate

# What is the n+1 select issue and how to find it

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