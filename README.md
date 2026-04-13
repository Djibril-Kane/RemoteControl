# RemoteControl - Logiciel de Contrôle à Distance

Logiciel de contrôle à distance en Java inspiré de SSH. Un client envoie des commandes à un serveur qui les exécute et retourne les résultats.

## Fonctionnalités

- Architecture client-serveur TCP
- Gestion multi-thread (plusieurs clients simultanés)
- Exécution de commandes système (Windows/Linux/macOS)
- Interface graphique Swing (ServerGUI, ClientGUI)
- Authentification par login/password (bonus)
- Journalisation avancée dans serveur.log (bonus)
- Historique des commandes (flèches haut/bas)

## Compilation & Exécution

```bash
# Compilation
javac *.java

# Serveur (Terminal 1)
java ServerApp

# Client (Terminal 2)
java ClientApp
```

## Structure du Projet

```
main/
├── ServerApp.java              # Point d'entrée serveur
├── ServerGUI.java              # Interface graphique serveur (Swing)
├── ServerListener.java         # Accepte les connexions TCP
├── ClientHandler.java          # Gère chaque client (Thread)
├── CommandExecutor.java        # Exécute les commandes système
├── ClientApp.java              # Point d'entrée client
├── ClientGUI.java              # Interface graphique client (Swing)
├── ClientNetwork.java          # Gère la connexion TCP
├── LoginGUI.java               # Authentification (Swing)
├── .gitignore
└── README.md
```

## Configuration

- Port serveur : 5000 (par défaut)
- Journalisation : `serveur.log`
- Protocole : TCP avec marqueur `--END--`
