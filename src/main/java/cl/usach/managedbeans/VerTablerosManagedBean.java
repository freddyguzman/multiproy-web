/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Tablero;
import cl.usach.sessionbeans.TableroFacadeLocal;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author FGT
 */
@Named(value = "verTablerosManagedBean")
@ViewScoped
public class VerTablerosManagedBean {
    @EJB
    private TableroFacadeLocal tableroFacade;

    List<Tablero> tableros;
    
    public VerTablerosManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        tableros = tableroFacade.findAll();
    }

    public List<Tablero> getTableros() {
        return tableros;
    }

    public void setTableros(List<Tablero> tableros) {
        this.tableros = tableros;
    }
    
}
