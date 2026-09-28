import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private int id;
    private Apprenant proprietaire;
    private LocalDate dateCreation;
    private LocalDate dateExpiration;
    private double prix;
    private int nombreRepasParJour;

    // Constructeurs
    public Ticket() {}

    public Ticket(Apprenant proprietaire, LocalDate dateCreation, LocalDate dateExpiration, double prix, int nombreRepasParJour) {
        this.proprietaire = proprietaire;
        this.dateCreation = dateCreation;
        this.dateExpiration = dateExpiration;
        this.prix = prix;
        this.nombreRepasParJour = nombreRepasParJour;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Apprenant getProprietaire() { return proprietaire; }
    public void setProprietaire(Apprenant proprietaire) { this.proprietaire = proprietaire; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getNombreRepasParJour() { return nombreRepasParJour; }
    public void setNombreRepasParJour(int nombreRepasParJour) { this.nombreRepasParJour = nombreRepasParJour; }

    // Enregistrer un nouveau ticket
    public boolean creer() {
        String sql = "INSERT INTO ticket (apprenant_id, date_creation, date_expiration, prix, nombre_repas_par_jour) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, this.proprietaire.getId());
            pstmt.setDate(2, Date.valueOf(this.dateCreation));
            pstmt.setDate(3, Date.valueOf(this.dateExpiration));
            pstmt.setDouble(4, this.prix);
            pstmt.setInt(5, this.nombreRepasParJour);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getInt(1);
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Ticket.creer) : " + e.getMessage());
        }
        return false;
    }

    // Rechercher un ticket par ID
    public static Ticket rechercherParId(int id) {
        String sql = "SELECT t.*, a.nom, a.prenom, a.email FROM ticket t " +
                "JOIN apprenant a ON t.apprenant_id = a.id WHERE t.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Apprenant app = new Apprenant(
                            rs.getInt("apprenant_id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email")
                    );

                    Ticket ticket = new Ticket(
                            app,
                            rs.getDate("date_creation").toLocalDate(),
                            rs.getDate("date_expiration").toLocalDate(),
                            rs.getDouble("prix"),
                            rs.getInt("nombre_repas_par_jour")
                    );
                    ticket.setId(rs.getInt("id"));
                    return ticket;
                }
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Ticket.rechercherParId) : " + e.getMessage());
        }
        return null;
    }

    // Vérifier si le ticket est toujours valide aujourd'hui
    public boolean estValide() {
        LocalDate aujourdhui = LocalDate.now();
        // 1. Vérification de la date de validité
        if (aujourdhui.isAfter(this.dateExpiration) || aujourdhui.isBefore(this.dateCreation)) {
            return false;
        }

        // 2. Vérification du quota journalier
        int consommationsAujourdhui = ServiceCantine.nombrePassagesAujourdhuiParTicket(this.id);
        return consommationsAujourdhui < this.nombreRepasParJour;
    }

    // Lister tous les tickets
    public static List<Ticket> listerTous() {
        List<Ticket> liste = new ArrayList<>();
        String sql = "SELECT t.*, a.nom, a.prenom, a.email FROM ticket t " +
                "JOIN apprenant a ON t.apprenant_id = a.id ORDER BY t.id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Apprenant app = new Apprenant(
                        rs.getInt("apprenant_id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")
                );

                Ticket t = new Ticket(
                        app,
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getDate("date_expiration").toLocalDate(),
                        rs.getDouble("prix"),
                        rs.getInt("nombre_repas_par_jour")
                );
                t.setId(rs.getInt("id"));
                liste.add(t);
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Ticket.listerTous) : " + e.getMessage());
        }
        return liste;
    }

    // Calculer le chiffre d'affaires total
    public static double calculerChiffreAffairesTotal() {
        String sql = "SELECT SUM(prix) FROM ticket";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Ticket.calculerChiffreAffairesTotal) : " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "Ticket N°" + id + " | Apprenant: " + proprietaire.getPrenom() + " " + proprietaire.getNom() +
                " | Début: " + dateCreation + " | Expiration: " + dateExpiration +
                " | Prix: " + prix + " FCFA | Quota/jour: " + nombreRepasParJour;
    }
}