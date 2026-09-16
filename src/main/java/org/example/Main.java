package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.example.model.Author;
import org.example.model.Book;
import org.example.model.Publisher;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        createEntities();

        selectAuthorsWithBooks();

        updateBookTitle();

        selectAuthorsWithBooks();

        deleteBook();

        selectAuthorsWithBooks();

        JpaUtil.shutdown();
    }

    // KREIRANJE ENTITETA

    public static void createEntities() {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            // Dva autora
            Author author1 = new Author("Ivo Andrić");
            Author author2 = new Author("Miroslav Krleža");

            // Dvije knjige
            Book book1 = new Book("Na Drini ćuprija");
            Book book2 = new Book("Povratak Filipa Latinovicza");

            // Dva izdavača
            Publisher publisher1 = new Publisher("Školska knjiga");
            Publisher publisher2 = new Publisher("Mladost");

            // Povezivanje autora i knjiga
            author1.addBook(book1);
            author2.addBook(book2);

            // Povezivanje knjiga i izdavača
            book1.addPublisher(publisher1);
            book1.addPublisher(publisher2);
            book2.addPublisher(publisher1);

            // Spremanje
            em.persist(author1);
            em.persist(author2);
            em.persist(publisher1);
            em.persist(publisher2);
            // Budući da Author ima cascade = ALL,
            // knjige će se također spremiti.
            em.getTransaction().commit();
            System.out.println("Entiteti su spremljeni.");
        } finally {
            em.close();
        }
    }

    // DOHVAĆANJE AUTORA I NJIHOVIH KNJIGA

    public static void selectAuthorsWithBooks() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery("FROM Author", Author.class);
            List<Author> authors = query.getResultList();
            System.out.println();
            System.out.println("Authors and their books:");

            for (Author author : authors) {
                System.out.println("Author: " + author.getName());
                for (Book book : author.getBooks()) {
                    System.out.println(" Book: " + book.getTitle() + ", Publishers: " + book.getPublishers().size());
                }
            }
        } finally {
            em.close();
        }
    }

    // AŽURIRANJE NASLOVA KNJIGE

    public static void updateBookTitle() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            Book book = em.find(Book.class, 1L);

            if (book != null) {
                System.out.println();
                System.out.println("Stari naslov: " + book.getTitle());
                book.setTitle("Na Drini ćuprija - novo izdanje");
                System.out.println("Novi naslov: " + book.getTitle());
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // BRISANJE KNJIGE
    public static void deleteBook() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Book book = em.find(Book.class, 2L);

            if (book != null) {
                // Uklanjamo knjigu iz autora
                if (book.getAuthor() != null) {
                    book.getAuthor().removeBook(book);
                }

                // Uklanjamo veze prema publisherima
                for (Publisher publisher : book.getPublishers()) {
                    publisher.getBooks().remove(book);
                }
                em.remove(book);
                System.out.println();
                System.out.println("Knjiga je obrisana.");
            }
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }
}