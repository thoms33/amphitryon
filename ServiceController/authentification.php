<?php
require_once '../lib/AutoLoader.php';


print(json_encode(UtilisateurDAO::authentification($_POST['login'], $_POST['mdp'])));
