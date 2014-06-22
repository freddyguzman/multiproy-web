/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Cuenta;
import cl.usach.entities.Equipo;
import cl.usach.entities.Tablero;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.CuentaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.TableroFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
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
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private CuentaFacadeLocal cuentaFacade;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private TableroFacadeLocal tableroFacade;

    List<Tablero> tableros;
    List<Equipo> equipos;
    
    private final SesionManagedBean sesionManagedBean = new SesionManagedBean();
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    
    public VerTablerosManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        actualizarTableros();
        
    }

    public List<Tablero> getTableros() {
        return tableros;
    }

    public void setTableros(List<Tablero> tableros) {
        this.tableros = tableros;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }
    
    public void actualizarTableros(){
        List<Equipo> auxE = new ArrayList<>();
        if(sesionManagedBean.checkUserAdmin()){
            tableros = tableroFacade.findAll();
            for (Tablero tablero : tableros) {
                if(equipoFacade.existeEquipoPorTablero(tablero)){
                    auxE.add(equipoFacade.buscarUnEquipoPoTablero(tablero));
                }                
            }
            equipos = auxE;
        }else{
            Usuario usuarioActual = usuarioFacade.buscarPorLogin(loginUsuario);
            List<Cuenta> cuentas = cuentaFacade.buscarPorUsuario(usuarioActual);
            for (Cuenta cuenta : cuentas) {
                auxE.addAll(equipoFacade.buscarPorCuenta(cuenta));
            }
            equipos = auxE;
        }        
    }
    
}
