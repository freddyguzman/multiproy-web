/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.converter.CodSHA;
import cl.usach.entities.Grupo;
import cl.usach.entities.RolUsuario;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.AsignaturaFacadeLocal;
import cl.usach.sessionbeans.GrupoFacadeLocal;
import cl.usach.sessionbeans.RolUsuarioFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author FGT
 */
@Named(value = "usuarioManagedBean")
@ViewScoped
public class UsuarioManagedBean {
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private AsignaturaFacadeLocal asignaturaFacade;
    @EJB
    private RolUsuarioFacadeLocal rolUsuarioFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    
    private Integer idUsuario;
    private String nombreUsuario;
    private String emailUsuario;
    private String loginUsuario;
    private String passUsuario;
    private RolUsuario idRolUsuario;
    private List<Usuario> usuarios;
    
    private Integer idUsuarioSeleccionado;
    private Integer idRolUsuarioSeleccionado;
    private Usuario usuarioSeleccionado;
    
    private List<Usuario> usuariosFiltrados;

    /**
     * Creates a new instance of UsuarioManagedBean
     */
    public UsuarioManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        usuarios = usuarioFacade.findAll();
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getLoginUsuario() {
        return loginUsuario;
    }

    public void setLoginUsuario(String loginUsuario) {
        this.loginUsuario = loginUsuario;
    }

    public String getPassUsuario() {
        return passUsuario;
    }

    public void setPassUsuario(String passUsuario) {
        this.passUsuario = passUsuario;
    }

    public RolUsuario getIdRolUsuario() {
        return idRolUsuario;
    }

    public void setIdRolUsuario(RolUsuario idRolUsuario) {
        this.idRolUsuario = idRolUsuario;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Integer getIdUsuarioSeleccionado() {
        return idUsuarioSeleccionado;
    }

    public void setIdUsuarioSeleccionado(Integer idUsuarioSeleccionado) {
        this.idUsuarioSeleccionado = idUsuarioSeleccionado;
    }

    public Integer getIdRolUsuarioSeleccionado() {
        return idRolUsuarioSeleccionado;
    }

    public void setIdRolUsuarioSeleccionado(Integer idRolUsuarioSeleccionado) {
        this.idRolUsuarioSeleccionado = idRolUsuarioSeleccionado;
    }

    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public List<Usuario> getUsuariosFiltrados() {
        return usuariosFiltrados;
    }

    public void setUsuariosFiltrados(List<Usuario> usuariosFiltrados) {
        this.usuariosFiltrados = usuariosFiltrados;
    }
    
    public void nuevoUsuario() throws NoSuchAlgorithmException{
        idRolUsuario = rolUsuarioFacade.buscarPorId(idRolUsuarioSeleccionado);
        CodSHA cod = new CodSHA();
        passUsuario = cod.codificarSHA256(passUsuario);
        Usuario usuario = new Usuario(nombreUsuario, emailUsuario, loginUsuario, passUsuario, idRolUsuario);
        
        FacesMessage msg = new FacesMessage("Usuario Agregado",usuario.getNombreUsuario());  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        usuarioFacade.create(usuario);
        usuarios = usuarioFacade.findAll();
        
        Grupo grupo = new Grupo(loginUsuario, idRolUsuario.getNombreRolUsuario());
        grupoFacade.create(grupo);
        limpiar();
    }
    
    public void onEdit(RowEditEvent event) throws Exception {
        Usuario usuario = (Usuario) event.getObject();        
        usuarioFacade.edit(usuario);
        FacesMessage msg = new FacesMessage("Usuario Editado");  
        FacesContext.getCurrentInstance().addMessage(null, msg);  
        
    }  
      
    public void onCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("No se realizó ningun cambio","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);  
    }
    
    public void onEliminar() {
        if(asignaturaFacade.existeAsignaturaPorUsuario(usuarioSeleccionado)){
            FacesMessage msg = new FacesMessage("No se puede eliminar usuario",usuarioSeleccionado.getNombreUsuario() + " contiene asignaturas asignadas");
            FacesContext.getCurrentInstance().addMessage(null, msg); 
        }else{
            Grupo grupo = grupoFacade.buscarPorNombreUsuario(usuarioSeleccionado.getLoginUsuario());
            grupoFacade.remove(grupo);
            
            usuarioFacade.remove(usuarioSeleccionado);           
            
            FacesMessage msg = new FacesMessage("Usuario Eliminado",usuarioSeleccionado.getNombreUsuario());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            usuarios = usuarioFacade.findAll();
        }        
    }
    
    public void limpiar(){
        idUsuario = null;
        nombreUsuario = null;
        emailUsuario = null;
        loginUsuario = null;
        passUsuario = null;
        idRolUsuario = null;
        idRolUsuarioSeleccionado = null;
    }
    
}
