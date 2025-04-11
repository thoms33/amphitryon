<?php
spl_autoload_register('Autoloader::autoloadDao');
spl_autoload_register('Autoloader::autoloadLib');


class Autoloader{
     
    private string $file;
    
    
    
    static function autoloadLib($class): void{
        $file =   $class . '.php';
        if(is_file($file)&& is_readable($file)){
            require $file;
        }
        
    }
    
    static function autoloadDao($class): void{
        $file = '../modeles/dao/' . $class . '.php';
        if(is_file($file)&& is_readable($file)){
            require $file;
        }
        
    }
    
    
}


