import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choix = -1;

        while (choix != 0) {
            afficherMenu();
            System.out.print("Faites votre choix : ");
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine(); // Consommer le retour à la ligne
                traiterChoix(choix);
            } else {
                System.out.println(" Veuillez entrer un nombre valide.");
                scanner.nextLine();
            }
        }
    }

    private static void afficherMenu() {
        System.out.println("\n=============================================");
        System.out.println("   GESTION CANTINE - ÉTABLISSEMENT LA PAIX   ");
        System.out.println("=============================================");
        System.out.println("1. Enregistrer un apprenant");
        System.out.println("2. Lister tous les apprenants");
        System.out.println("3. Acheter / Enregistrer un ticket");
        System.out.println("4. Lister tous les tickets");
        System.out.println("5. Valider un passage à la cantine (Servir un repas)");
        System.out.println("6. Voir les statistiques (Chiffre d'affaires & Repas du jour)");
        System.out.println("0. Quitter");
        System.out.println("=============================================");
    }

    private static void traiterChoix(int choix) {
        switch (choix) {
            case 1:
                enregistrerApprenant();
                break;
            case 2:
                listerApprenants();
                break;
            case 3:
                acheterTicket();
                break;
            case 4:
                listerTickets();
                break;
            case 5:
                validerPassageCantine();
                break;
            case 6:
                afficherStatistiques();
                break;
            case 0:
                System.out.println("Au revoir et à bientôt !");
                break;
            default:
                System.out.println(" Option invalide. Veuillez réessayer .");
        }
    }

    // 1. Enregistrer un apprenant
    private static void enregistrerApprenant() {
        System.out.println("\n--- ENREGISTREMENT D'UN APPRENANT ---");
        System.out.print("Nom : ");
        String nom = scanner.nextLine();
        System.out.print("Prénom : ");
        String prenom = scanner.nextLine();
        System.out.print("Email : ");
        String email = scanner.nextLine();

        Apprenant apprenant = new Apprenant(nom, prenom, email);
        if (apprenant.creer()) {
            System.out.println(" Apprenant enregistré avec succès ! ID attribué : " + apprenant.getId());
        } else {
            System.out.println(" Échec de l'enregistrement de l'apprenant.");
        }
    }

    // 2. Lister tous les apprenants
    private static void listerApprenants() {
        System.out.println("\n--- LISTE DES APPRENANTS ---");
        List<Apprenant> liste = Apprenant.listerTous();
        if (liste.isEmpty()) {
            System.out.println("Aucun apprenant trouvé.");
        } else {
            for (Apprenant a : liste) {
                System.out.println(a);
            }
        }
    }

    // 3. Acheter un ticket
    private static void acheterTicket() {
        System.out.println("\n--- ACHAT D'UN TICKET ---");
        System.out.print("Saisir l'ID de l'apprenant : ");
        int idApprenant = scanner.nextInt();
        scanner.nextLine();

        Apprenant apprenant = Apprenant.rechercherParId(idApprenant);
        if (apprenant == null) {
            System.out.println(" Apprenant introuvable avec l'ID " + idApprenant);
            return;
        }

        System.out.print("Durée de validité (en jours) : ");
        int dureeJours = scanner.nextInt();

        System.out.print("Prix du ticket (FCFA) : ");
        double prix = scanner.nextDouble();

        System.out.print("Nombre de repas autorisés par jour : ");
        int repasParJour = scanner.nextInt();
        scanner.nextLine();

        LocalDate dateCreation = LocalDate.now();
        LocalDate dateExpiration = dateCreation.plusDays(dureeJours);

        Ticket ticket = new Ticket(apprenant, dateCreation, dateExpiration, prix, repasParJour);
        if (ticket.creer()) {
            System.out.println(" Ticket N°" + ticket.getId() + " créé avec succès pour " + apprenant.getPrenom() + " " + apprenant.getNom());
            System.out.println("   Valide du " + dateCreation + " au " + dateExpiration);
        } else {
            System.out.println(" Échec de la création du ticket.");
        }
    }

    // 4. Lister tous les tickets
    private static void listerTickets() {
        System.out.println("\n--- LISTE DES TICKETS ---");
        List<Ticket> liste = Ticket.listerTous();
        if (liste.isEmpty()) {
            System.out.println("Aucun ticket enregistré.");
        } else {
            for (Ticket t : liste) {
                System.out.println(t);
            }
        }
    }

    // 5. Valider le passage à la cantine
    private static void validerPassageCantine() {
        System.out.println("\n--- PASSAGE À LA CANTINE ---");
        System.out.print("Saisir le N° du ticket : ");
        int idTicket = scanner.nextInt();
        scanner.nextLine();

        Ticket ticket = Ticket.rechercherParId(idTicket);
        if (ticket == null) {
            System.out.println(" Ticket introuvable.");
            return;
        }

        if (ticket.estValide()) {
            ServiceCantine passage = new ServiceCantine(ticket);
            if (passage.enregistrer()) {
                passage.genererRecuTextuel();
            } else {
                System.out.println(" Erreur lors de l'enregistrement du passage.");
            }
        } else {
            System.out.println(" PASSAGE REFUSÉ :  le quota de repas du jour a été atteint !");
        }
    }

    // 6. Afficher les statistiques
    private static void afficherStatistiques() {
        System.out.println("\n--- STATISTIQUES ---");
        double caTotal = Ticket.calculerChiffreAffairesTotal();
        int repasAujourdhui = ServiceCantine.nombreRepasServisAujourdhui();

        System.out.println(" Chiffre d'affaires total : " + caTotal + " FCFA");
        System.out.println("  Repas servis aujourd'hui : " + repasAujourdhui);
    }
}