/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Asignatura;
import cl.usach.entities.DetalleUsuarioTarjeta;
import cl.usach.entities.Equipo;
import cl.usach.entities.Miembro;
import cl.usach.entities.SprintAsignatura;
import cl.usach.entities.SprintGrupos;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.AsignaturaFacadeLocal;
import cl.usach.sessionbeans.DetalleUsuarioTarjetaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.MiembroFacadeLocal;
import cl.usach.sessionbeans.SprintAsignaturaFacadeLocal;
import cl.usach.sessionbeans.SprintGruposFacadeLocal;
import cl.usach.sessionbeans.TableroFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Seconds;

/**
 *
 * @author FGT
 */
@Named(value = "creditosManagedBean")
@ViewScoped
public class CreditosManagedBean {
    @EJB
    private DetalleUsuarioTarjetaFacadeLocal detalleUsuarioTarjetaFacade;
    @EJB
    private MiembroFacadeLocal miembroFacade;
    @EJB
    private TableroFacadeLocal tableroFacade;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private SprintGruposFacadeLocal sprintGruposFacade;
    @EJB
    private SprintAsignaturaFacadeLocal sprintAsignaturaFacade;
    @EJB
    private AsignaturaFacadeLocal asignaturaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    
    private List<Asignatura> asignaturas;
    
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    
    public CreditosManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        Usuario usuario = usuarioFacade.buscarPorLogin(loginUsuario);
        asignaturas = asignaturaFacade.buscarPorIdUsuario(usuario);
    }

    public List<Asignatura> getAsignaturas() {
        return asignaturas;
    }
    
    public List<SprintAsignatura> buscarSprintAsignaturas(Asignatura asignatura){
        return sprintAsignaturaFacade.buscarPorAsignatura(asignatura);
    } 
    
    public List<SprintGrupos> buscarSprintGrupos(SprintAsignatura sprintA){
        return sprintGruposFacade.buscarPorSprintAsignatura(sprintA);
    }
    
    public List<Equipo> buscarEquipos(SprintGrupos sprintG){
        return equipoFacade.buscarPorIdSprintGrupo(sprintG);
    }
    
    public int buscarCantidadTareas(Equipo equipo){
        List<Miembro> miembros = miembroFacade.buscarPoIdTableroYIdCuenta(equipo.getIdTablero(), equipo.getIdCuenta());
        if(!miembros.isEmpty() && miembros.size() == 1 ){
            return detalleUsuarioTarjetaFacade.buscarPorIdMiembroYIdTablero(miembros.get(0), equipo.getIdTablero()).size();
        }
        return 0;
    }
    
    public int buscarTiempoTareas(Equipo equipo){
        int segundos = 0;
        List<Miembro> miembros = miembroFacade.buscarPoIdTableroYIdCuenta(equipo.getIdTablero(), equipo.getIdCuenta());
        if(!miembros.isEmpty() && miembros.size() == 1 ){
            List<DetalleUsuarioTarjeta> detallesT = detalleUsuarioTarjetaFacade.buscarPorIdMiembroYIdTablero(miembros.get(0), equipo.getIdTablero());
            for (DetalleUsuarioTarjeta detalleT : detallesT) {
                switch (detalleT.getIdTarjeta().getIdEstadoTarjeta().getNombreEstadoTarjeta()) {
                    case "En proceso":
                        segundos += obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(),new Date());
                        break;
                    case "Terminada":
                        segundos += obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(),
                                detalleT.getIdTarjeta().getFechaFinalTarjeta());
                        break;
                }
            }
        }
        return segundos;
    }
    
    public String buscarTiempoTareasFormato(Equipo equipo){
        int segundos = buscarTiempoTareas(equipo);
        return obtenerTiempoProyecto(segundos);
    }
    
    public double buscarPromedioSrpintGrupo(SprintGrupos springG){
        List<Equipo> eqs = buscarEquipos(springG);
        SummaryStatistics stats = new SummaryStatistics();
        int s;
        for (Equipo equipo : eqs) {            
            s = buscarTiempoTareas(equipo);
            stats.addValue(s);
        }
        double mean = stats.getMean();
        mean = (double) Math.round(mean * 10)/10;
        return mean;
    }
    
    public double buscarDesviacionStandarSrpintGrupo(SprintGrupos sprintG){
        List<Equipo> eqs = buscarEquipos(sprintG);
        SummaryStatistics stats = new SummaryStatistics();
        int s;
        for (Equipo equipo : eqs) {            
            s = buscarTiempoTareas(equipo);
            stats.addValue(s);
        }
        double dv = stats.getStandardDeviation();
        dv = (double) Math.round(dv * 10)/10;
        return dv;
    }
    
    public double buscarPromedioSprintAsignatura(SprintAsignatura sprintA){
        List<SprintGrupos> spgs = buscarSprintGrupos(sprintA);
        SummaryStatistics stats = new SummaryStatistics();
        int a;
        for (SprintGrupos sprintGrupos : spgs) {
            List<Equipo> eqs = buscarEquipos(sprintGrupos);
            for (Equipo equipo : eqs) {
                a = buscarTiempoTareas(equipo);
                stats.addValue(a);
            }            
        }
        double mean = stats.getMean();
        mean = (double) Math.round(mean * 10)/10;
        return mean;
    }
    
    public double buscarDesviacionEstandarSprintAsignatura(SprintAsignatura sprintA){
        List<SprintGrupos> spgs = buscarSprintGrupos(sprintA);
        SummaryStatistics stats = new SummaryStatistics();
        int a;
        for (SprintGrupos sprintGrupos : spgs) {
            List<Equipo> eqs = buscarEquipos(sprintGrupos);
            for (Equipo equipo : eqs) {
                a = buscarTiempoTareas(equipo);
                stats.addValue(a);
            }            
        }
        
        double dv = stats.getStandardDeviation();
        dv = (double) Math.round(dv * 10)/10;
        return dv;
    }
    
    public int obtenerHorasDiff(Date inicio, Date fin){
        DateTime dtI = new DateTime(inicio);
        DateTime dtF = new DateTime(fin);        
        int tiempo = Hours.hoursBetween(dtI, dtF).getHours(); 
        return tiempo;
    }
    
    public String obtenerTiempoProyecto(int seg){
        long secondsTime = seg;
        int segundos = (int) secondsTime % 60;
        secondsTime -= segundos;
        long minutesTime = secondsTime / 60;
        long minutos = minutesTime % 60;
        minutesTime -= minutos;
        long hoursTime = minutesTime / 60;
        long horas = hoursTime % 24;
        hoursTime -= horas;
        long dias = hoursTime / 24;
        String tiempo = "" + dias + " dia(s), " + horas + " hora(s), " + minutos +  " minuto(s), " + segundos + " segundo(s)";
                
        return tiempo;
    }
    
    public String formatFecha(Date fecha){
        if(fecha == null) return "-";        
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String datef = df.format(fecha);
        return datef;
    }
    
    /*public int obtenerCantidadSemanas(Date dateI, Date dateF){
        DateTime dateTime1 = new DateTime(dateI);
        DateTime dateTime2 = new DateTime(dateF);
        return Weeks.weeksBetween(dateTime1, dateTime2).getWeeks();
    }
    
    public List<String> obtenerSemanasSprint(SprintAsignatura sprintA){
        List<String> aux = new ArrayList<>();
        System.out.println(sprintA.getFechaInicioSprintAsignatura()+ " -- "+sprintA.getFechaTerminoSprintAsignatura());
        System.out.println(obtenerCantidadSemanas(sprintA.getFechaInicioSprintAsignatura(),sprintA.getFechaTerminoSprintAsignatura()));
        
        return aux;
    }*/
        
}
