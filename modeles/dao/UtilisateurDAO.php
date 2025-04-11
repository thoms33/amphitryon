<?php
class UtilisateurDAO{
	public static function authentification($login , $mdp){
		try{
			$sql = "select idUtilisateur, nomUtilisateur , prenomUtilisateur,idRole from Utilisateur 
			where loginUtilisateur= :login and mdpUtilisateur = :mdp " ;
			$requetePrepa = DBConnex::getInstance()->prepare($sql);
			//$mdp =  md5($mdp);
			$requetePrepa->bindParam("login", $login);
			$requetePrepa->bindParam("mdp", $mdp);
			$requetePrepa->execute();
			$reponse = $requetePrepa->fetch(PDO::FETCH_ASSOC);
		}catch(Exception $e){
			//$reponse = "";
			$reponse = $e->getMessage();
		}
		return $reponse;
	}
}

?>