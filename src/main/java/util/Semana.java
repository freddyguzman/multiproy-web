/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.util.Date;

/**
 *
 * @author FGT
 */
public class Semana {
    private Date inicio;
    private Date termino;

    public Semana(Date inicio, Date termino) {
        this.inicio = inicio;
        this.termino = termino;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getTermino() {
        return termino;
    }

    public void setTermino(Date termino) {
        this.termino = termino;
    }

    @Override
    public String toString() {
        return inicio + " - " + termino; 
    }
    
    
}
