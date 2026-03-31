# RemoteControl - Logiciel de Contrôle à Distance

Logiciel de contrôle à distance en Java fonctionnant selon une architecture client-serveur (similaire à SSH).

## Description

- **Client** : envoie des commandes système au serveur
- **Serveur** : reçoit les commandes, les exécute et retourne les résultats

## Fonctionnalités

- Communication TCP/IP (Sockets)
- Gestion de plusieurs clients simultanés (Threads)
- Exécution de commandes système (Windows, Linux, macOS)
- Affichage des résultats et erreurs

## Compilation

```bash
javac *.java
```

## Exécution

**Démarrer le serveur :**
```bash
java Server
```

**Démarrer le client :**
```bash
java Client
```

## Structure du Projet

```
RemoteControl/
├── Constants.java        # Constantes du projet
├── CommandExecutor.java  # Exécution des commandes
├── Server.java          # Serveur TCP
├── ClientHandler.java   # Gestion des clients
├── Client.java          # Client TCP
├── .gitignore
└── README.md
```

## Groupe

À remplir avec les noms des 3 étudiants

## Date de Rendu

12 avril 2026
