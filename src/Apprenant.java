import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Apprenant {
    private int id;
    private String nom;
    private String prenom;
    private String email;

    // Constructeurs
    public Apprenant() {}

    public Apprenant(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public Apprenant(int id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Enregistrer un apprenant
    public boolean creer() {
        String sql = "INSERT INTO apprenant (nom, prenom, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, this.nom);
            pstmt.setString(2, this.prenom);
            pstmt.setString(3, this.email);

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
            System.err.println(" Erreur BDD (Apprenant.creer) : " + e.getMessage());
        }
        return false;
    }

    // Lister tous les apprenants
    public static List<Apprenant> listerTous() {
        List<Apprenant> liste = new ArrayList<>();
        String sql = "SELECT * FROM apprenant ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Apprenant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Apprenant.listerTous) : " + e.getMessage());
        }
        return liste;
    }

    // Rechercher un apprenant par ID
    public static Apprenant rechercherParId(int id) {
        String sql = "SELECT * FROM apprenant WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Apprenant(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (Apprenant.rechercherParId) : " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return "Apprenant [ID=" + id + ", Nom=" + nom + ", Prénom=" + prenom + ", Email=" + email + "]";
    }
}