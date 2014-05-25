/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.converter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author FGT
 */
public class CodSHA {
    
    public String codificarSHA256(String pass) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(pass.getBytes());
        
        byte byteData[] = md.digest();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
