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
import cl.usach.kanbanizesessionbeans.TableroKanbanizeLocal;
import cl.usach.trellosessionbeans.TableroTrelloLocal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author FGT
 */
@Named(value = "tableroManagedBean")
@ViewScoped
public class TableroManagedBean {
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private TableroFacadeLocal tableroFacade;
    @EJB
    private TableroKanbanizeLocal tableroKanbanize;
    @EJB
    private TableroTrelloLocal tableroTrello;
    @EJB
    private CuentaFacadeLocal cuentaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    
    private final SesionManagedBean sesionManagedBean = new SesionManagedBean();
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    
    private List<Equipo> equipos;
    private Equipo equipoSeleccionado;
    
    public TableroManagedBean() {
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    public Equipo getEquipoSeleccionado() {
        return equipoSeleccionado;
    }

    public void setEquipoSeleccionado(Equipo equipoSeleccionado) {
        this.equipoSeleccionado = equipoSeleccionado;
    }   
        
    public void buscarTableros(){
        List<Equipo> auxE = new ArrayList<>();
        List<Usuario> usuarios = buscarUsuarios();
        for (Usuario usuario : usuarios) {
            List<Cuenta> cuentas = cuentaFacade.buscarPorUsuario(usuario);
            for (Cuenta cuenta : cuentas) {
                if("Trello".equals(cuenta.getIdTipoCuenta().getNombreTipoCuenta())){
                    auxE.addAll(tableroTrello.buscarTableros(cuenta));
                }
                if("Kanbanize".equals(cuenta.getIdTipoCuenta().getNombreTipoCuenta())){
                    auxE.addAll(tableroKanbanize.buscarTableros(cuenta));
                }
            }
        }
        
        equipos = auxE;
    }
    
    public Boolean existeTablero(String idTableroExt){
        return tableroFacade.existeTableroPorIdTableroExt(idTableroExt);        
    }
    
    public Boolean existeEquipo(Equipo equipo){
        return equipoFacade.existeEquipoPorCuentaYTablero(equipo.getIdCuenta(), equipo.getIdTablero().getIdTableroExt());
    }
    
    public void nuevoTablero(){        
        if(tableroFacade.existeTableroPorIdTableroExt(equipoSeleccionado.getIdTablero().getIdTableroExt())){
            Tablero tab = tableroFacade.buscarPorIdTableroExt(equipoSeleccionado.getIdTablero().getIdTableroExt());
            Equipo equipo = new Equipo(equipoSeleccionado.getIdCuenta(), tab);
            equipoFacade.create(equipo);
        }else{
            tableroFacade.create(equipoSeleccionado.getIdTablero());            
            Tablero tab = tableroFacade.buscarPorIdTableroExt(equipoSeleccionado.getIdTablero().getIdTableroExt());
            Equipo equipo = new Equipo(equipoSeleccionado.getIdCuenta(), tab);
            equipoFacade.create(equipo);
        }
        
        FacesMessage msg = new FacesMessage("Tablero Agregado",equipoSeleccionado.getIdTablero().getNombreTablero());
        FacesContext.getCurrentInstance().addMessage(null, msg);               
    }
    
    public void eliminarTablero(){
        Equipo equ = equipoFacade.buscarPorCuentaYTablero(equipoSeleccionado.getIdCuenta(), equipoSeleccionado.getIdTablero().getIdTableroExt());
        equipoFacade.remove(equ);
        if(!equipoFacade.existeEquipoPorTablero(equ.getIdTablero())){
            tableroFacade.remove(equ.getIdTablero());
        }
        
        FacesMessage msg = new FacesMessage("Tablero Eliminado",equipoSeleccionado.getIdTablero().getNombreTablero());  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public List<Usuario> buscarUsuarios(){
        if(sesionManagedBean.checkUserAdmin()){
           return usuarioFacade.findAll(); 
        }else{
           Usuario usuarioActual = usuarioFacade.buscarPorLogin(loginUsuario);
           List<Usuario> usuarios = new ArrayList<>();
           usuarios.add(usuarioActual);
           return usuarios;
        }
    }
}
