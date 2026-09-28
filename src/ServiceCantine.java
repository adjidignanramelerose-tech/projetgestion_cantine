import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServiceCantine {
    private int id;
    private Ticket ticket;
    private LocalDate dateService;
    private LocalTime heureService;

    // Constructeurs
    public ServiceCantine() {}

    public ServiceCantine(Ticket ticket) {
        this.ticket = ticket;
        this.dateService = LocalDate.now();
        this.heureService = LocalTime.now();
    }

    public ServiceCantine(int id, Ticket ticket, LocalDate dateService, LocalTime heureService) {
        this.id = id;
        this.ticket = ticket;
        this.dateService = dateService;
        this.heureService = heureService;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public LocalDate getDateService() { return dateService; }
    public void setDateService(LocalDate dateService) { this.dateService = dateService; }

    public LocalTime getHeureService() { return heureService; }
    public void setHeureService(LocalTime heureService) { this.heureService = heureService; }

    // Enregistrer le passage dans la table 'service'
    public boolean enregistrer() {
        String sql = "INSERT INTO service (ticket_id, date_service, heure_service) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, this.ticket.getId());
            pstmt.setDate(2, Date.valueOf(this.dateService));
            pstmt.setTime(3, Time.valueOf(this.heureService));

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
            System.err.println(" Erreur BDD (ServiceCantine.enregistrer) : " + e.getMessage());
        }
        return false;
    }

    // Compter les repas consommés aujourd'hui pour un ticket
    public static int nombrePassagesAujourdhuiParTicket(int ticketId) {
        String sql = "SELECT COUNT(*) FROM service WHERE ticket_id = ? AND date_service = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (nombrePassagesAujourdhuiParTicket) : " + e.getMessage());
        }
        return 0;
    }

    // Compter le nombre total de repas servis aujourd'hui
    public static int nombreRepasServisAujourdhui() {
        String sql = "SELECT COUNT(*) FROM service WHERE date_service = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(" Erreur BDD (nombreRepasServisAujourdhui) : " + e.getMessage());
        }
        return 0;
    }

    // Génération du reçu au format texte
    public void genererRecuTextuel() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        System.out.println("\n---------------------------------------------");
        System.out.println("        REÇU DE PASSAGE - CANTINE           ");
        System.out.println("---------------------------------------------");
        System.out.println(" Passage N°    : " + this.id);
        System.out.println(" Date          : " + this.dateService.format(dateFormatter));
        System.out.println(" Heure         : " + this.heureService.format(timeFormatter));
        System.out.println(" Ticket N°     : " + this.ticket.getId());
        System.out.println(" Apprenant     : " + this.ticket.getProprietaire().getPrenom() + " " + this.ticket.getProprietaire().getNom());
        System.out.println(" Statut        : REPAS VALIDE ET SERVI");
        System.out.println("---------------------------------------------\n");
    }

    @Override
    public String toString() {
        return "ServiceCantine [Passage N°=" + id + ", Ticket N°=" + ticket.getId() + ", Date=" + dateService + ", Heure=" + heureService + "]";
    }
}