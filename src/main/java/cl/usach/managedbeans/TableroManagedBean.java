/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Cuenta;
import cl.usach.entities.Equipo;
import cl.usach.entities.SprintGrupos;
import cl.usach.entities.Tablero;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.CuentaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.TableroFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import cl.usach.kanbanizesessionbeans.TableroKanbanizeLocal;
import cl.usach.sessionbeans.SprintGruposFacadeLocal;
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
    private SprintGruposFacadeLocal sprintGruposFacade;
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
    private SprintGrupos sprintGrupoSeleccionado;
    private int idSprintGrupoSeleccionado;
    
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

    public SprintGrupos getSprintGrupoSeleccionado() {
        return sprintGrupoSeleccionado;
    }

    public void setSprintGrupoSeleccionado(SprintGrupos sprintGrupoSeleccionado) {
        this.sprintGrupoSeleccionado = sprintGrupoSeleccionado;
    }

    public int getIdSprintGrupoSeleccionado() {
        return idSprintGrupoSeleccionado;
    }

    public void setIdSprintGrupoSeleccionado(int idSprintGrupoSeleccionado) {
        this.idSprintGrupoSeleccionado = idSprintGrupoSeleccionado;
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
            Tablero tab = equ.getIdTablero();
            if(tab.getIdSprintGrupo() == null){
                tableroFacade.remove(equ.getIdTablero());
            }            
            
            int pos = equipos.indexOf(equipoSeleccionado);
            if(pos != -1){
                Equipo auxE = equipos.get(pos);
                auxE.getIdTablero().setIdSprintGrupo(null);
                idSprintGrupoSeleccionado = 0;
            }
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
    
    public Boolean verificarTableroyScrumMaster(Equipo equipo){
        Usuario usuarioActual = usuarioFacade.buscarPorLogin(loginUsuario);
        Boolean check = equipoFacade.existeEquipoPorCuentaYTablero(equipo.getIdCuenta(), equipo.getIdTablero().getIdTableroExt())
                && sprintGruposFacade.existeSprintGrupoPorUsuario(usuarioActual);
        
        return check;
    }
    
    public List<SprintGrupos> buscarSprintsGruposPorUsuario(){
        Usuario usuarioActual = usuarioFacade.buscarPorLogin(loginUsuario);
        return sprintGruposFacade.buscarPorUsuario(usuarioActual);
    }
    
    public String buscarAsignatura(Equipo equipo){
        if(existeEquipo(equipo)){
            Equipo eq = equipoFacade.buscarPorCuentaYTablero(equipo.getIdCuenta(), equipo.getIdTablero().getIdTableroExt());
            if(eq.getIdTablero().getIdSprintGrupo() != null){
                return eq.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getIdAsignatura().getNombreAsignatura()
                        + " / " + eq.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getNombreSprintAsignatura()
                        + " / " + eq.getIdTablero().getIdSprintGrupo().getNombreSprintGrupo();
            }
        }       
        return "No existe asignatura asignada";
    }
    
    public void asignarAsignatura(Equipo equipo){
        if(idSprintGrupoSeleccionado != -1){
            SprintGrupos sg = sprintGruposFacade.buscarPorId(idSprintGrupoSeleccionado);
            Tablero tablero = equipo.getIdTablero();
            tablero.setIdSprintGrupo(sg);
            tableroFacade.edit(tablero);
            FacesMessage msg = new FacesMessage("Asignatura Asignada","");  
            FacesContext.getCurrentInstance().addMessage(null, msg); 
        }else{
            Tablero tablero = equipo.getIdTablero();
            tablero.setIdSprintGrupo(null);
            tableroFacade.edit(tablero);
            FacesMessage msg = new FacesMessage("No se ha asignado ninguna asignatura","");  
            FacesContext.getCurrentInstance().addMessage(null, msg); 
        }
        
    }
    
    public void buscarTableroReal(){
        equipoSeleccionado = equipoFacade.buscarPorCuentaYTablero(equipoSeleccionado.getIdCuenta(), equipoSeleccionado.getIdTablero().getIdTableroExt());
        if(equipoSeleccionado.getIdTablero().getIdSprintGrupo() != null){
           idSprintGrupoSeleccionado = equipoSeleccionado.getIdTablero().getIdSprintGrupo().getIdSprintGrupo(); 
        }
    }
}
