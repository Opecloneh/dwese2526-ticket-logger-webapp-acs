package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceCreateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDetailDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceUpdateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.ProvinceService;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.RegionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

// Activa Mockito en JUnit 5:
// inicializa @Mock y @InjectMocks automáticamente (modo strict stubs)
@ExtendWith(MockitoExtension.class)
class ProvinceControllerTest {


    @Mock
    private RegionService regionService;

    @Mock // Creamos un mock del service para evitar tocar BD o lógica real
    private ProvinceService provinceService;


    @Mock // Creamos un mock de MessageSource para controlar los mensajes i18n que devuelve el controlador
    private MessageSource messageSource;


    @InjectMocks // Creamos el controlador REAL e inyectamos dentro provinceService y messageSource mockeados
    private ProvinceController controller;



    /**
     * Crea un Pageable típico como el del controlador (size=10, sort=name ASC).
     */
    private Pageable defaultPageable() {
        return PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"))); // Página 0, tamaño 10, orden por name ascendente
    }

    private Page<ProvinceDTO> samplePage() {
        // Creamos una Page “falsa” con 2 DTOs para simular lo que devolvería el service en un listado
        return new PageImpl<>(List.of(new ProvinceDTO(), new ProvinceDTO()), defaultPageable(), 2);
        // - contenido: 2 elementos
        // - pageable: el mismo pageable que usamos en tests
        // - totalElements: 2 (total real de elementos)
    }


    /**
     * Devuelve un objeto "dummy" compatible con el tipo de retorno real de ProvinceService.listRegionsForSelect().
     * <p>
     * Por qué existe:
     * - No queremos acoplar el test al tipo concreto (List, Iterable, Collection, DTO, Entity, etc.).
     * - Queremos que compile aunque cambies la firma del método en el futuro.
     */
    private Object dummyRegionsReturnValueCompatibleWithServiceSignature() {
        try {
            // Buscamos por reflection el método listRegionsForSelect() en la interfaz/clase ProvinceService
            Method m = ProvinceService.class.getMethod("listRegionsForSelect");
            // Obtenemos el tipo de retorno real del método
            Class<?> returnType = m.getReturnType();


            // Si el método devuelve algo que implementa Iterable (List, Set, etc.), podemos devolver una lista vacía
            if (Iterable.class.isAssignableFrom(returnType)) {
                return List.of(); // List implementa Iterable, así que “cuadra” con Iterable
            }


            // Si devuelve Object (muy genérico), también devolvemos una lista vacía
            if (returnType.equals(Object.class)) {
                return List.of();
            }


            // Si devuelve un tipo concreto (por ejemplo un DTO propio), devolvemos un mock de ese tipo
            // para que sea “instanciable” sin saber cómo construirlo realmente
            return mock(returnType);


        } catch (NoSuchMethodException e) {
            // Si alguien renombra el método o lo borra, queremos fallar el test de manera clara
            fail("No existe ProvinceService.listRegionsForSelect(). Ajusta el helper del test.");
            return null; // No se alcanzará porque fail() lanza AssertionError, pero Java lo exige
        }
    }

    /**
     * Stub robusto: hace que provinceService.listRegionsForSelect() devuelva algo compatible con la firma.
     */
    private void stubListRegionsForSelect() {
        // Usamos doAnswer porque el retorno puede ser genérico o variar y así evitamos problemas de generics con thenReturn(...)
        doAnswer(inv -> dummyRegionsReturnValueCompatibleWithServiceSignature())
                .when(provinceService).listRegionsForSelect();
        // Resultado: cada vez que el controlador llame a listRegionsForSelect(), el mock devolverá algo “compatible”
    }
// =========================
// GET /provinces  (listProvinces)
// =========================


    @Test // Marca este método como test
    @DisplayName("listProvinces OK -> devuelve vista listado y mete page + sortParam")
        // Nombre legible del caso de prueba
    void listProvinces_ok() {
        Pageable pageable = defaultPageable(); // Preparamos un Pageable como el que usa el controlador
        Model model = new ExtendedModelMap();  // Usamos un Model real para poder inspeccionar atributos
        Locale locale = new Locale("es");      // Simulamos que el idioma activo es español


        when(provinceService.list(pageable))   // Definimos el comportamiento del mock: si se llama list(pageable)...
                .thenReturn(samplePage());     // ...devolverá una Page con 2 provincias simuladas


        String view = controller.listProvinces(pageable, model, locale); // Llamamos directamente al método del controlador


        assertEquals("views/province/province-list", view); // Comprobamos que la vista devuelta es la esperada
        assertTrue(model.containsAttribute("page"));        // Comprobamos que el controlador mete la página en el model
        assertEquals("name,asc", model.getAttribute("sortParam")); // Verificamos que calcula correctamente sortParam
        verify(provinceService).list(pageable); // Verificamos que realmente llamó al service (interacción esperada)
    }
// =========================
// GET /provinces/new  (showNewForm)
// =========================


    @Test
    @DisplayName("showNewForm OK -> devuelve form y carga province + listRegions")
    void showNewForm_ok() {
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es");     // Locale español


        stubListRegionsForSelect(); // Preparamos el mock para que listRegionsForSelect() devuelva algo compatible


        String view = controller.showNewForm(model, locale); // Llamamos al método del controlador


        assertEquals("views/province/province-form", view); // Debe devolver la vista del formulario
        assertTrue(model.containsAttribute("province"));     // Debe meter un objeto province vacío en el model
        assertTrue(model.containsAttribute("listRegions"));  // Debe meter la lista de regiones en el model
        verify(provinceService).listRegionsForSelect();      // Debe haber pedido las regiones al service
    }
// =========================
// POST /provinces/insert  (insertProvince)
// =========================


    @Test
    @DisplayName("insertProvince OK -> create y redirect listado")
    void insert_ok() {
        ProvinceCreateDTO dto = mock(ProvinceCreateDTO.class); // Creamos un mock del DTO para controlar getters


        BindingResult br = mock(BindingResult.class); // Mock de BindingResult (resultado de validación del form)
        when(br.hasErrors()).thenReturn(false); // Simulamos: NO hay errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real por si el controlador añade flashes
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale de prueba


        String view = controller.insertProvince(dto, br, ra, model, locale); // Ejecutamos el método insert


        assertEquals("redirect:/provinces", view); // Si va bien, redirige al listado
        verify(provinceService).create(dto); // Debe llamar al service para crear la provincia
    }

    @Test
    @DisplayName("insertProvince VALIDATION -> vuelve a form y recarga listRegions")
    void insert_validationErrors() {
        ProvinceCreateDTO dto = mock(ProvinceCreateDTO.class); // DTO mockeado (no importa el contenido aquí)


        BindingResult br = mock(BindingResult.class); // BindingResult mockeado
        when(br.hasErrors()).thenReturn(true); // Simulamos: SÍ hay errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        stubListRegionsForSelect(); // Si hay errores, el controlador suele volver al form y necesita recargar regiones


        String view = controller.insertProvince(dto, br, ra, model, locale); // Ejecutamos el método insert


        assertEquals("views/province/province-form", view); // Con errores vuelve al formulario
        assertTrue(model.containsAttribute("listRegions"));  // Debe recargar listRegions para que el select funcione
        verify(provinceService).listRegionsForSelect();      // Debe llamar al service para obtener regiones
        verify(provinceService, never()).create(any());      // Con errores NO debe intentar crear nada
    }
// =========================
// POST /provinces/delete  (deleteProvince)
// =========================


    @Test
    @DisplayName("deleteProvince OK -> delete y redirect listado")
    void delete_ok() {
        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real
        Locale locale = new Locale("es"); // Locale


        String view = controller.deleteProvince(1L, ra, locale); // Ejecutamos delete


        assertEquals("redirect:/provinces", view); // Redirige al listado
        verify(provinceService).delete(1L); // Debe llamar al service para borrar
    }

    @Test
    @DisplayName("deleteProvince NOT FOUND -> redirect listado + flash")
    void delete_notFound() {
        RedirectAttributes ra = new RedirectAttributesModelMap(); // Para flash attributes
        Locale locale = new Locale("es"); // Locale


        doThrow(new ResourceNotFoundException("Province", "id", 99L))
                // Simulamos que el borrado falla por “no encontrada”
                .when(provinceService).delete(99L);


        when(messageSource.getMessage(eq("msg.province-controller.detail.notFound"), any(), eq(locale)))
                // Mensaje i18n
                .thenReturn("Provincia no encontrada");


        String view = controller.deleteProvince(99L, ra, locale); // Ejecutamos delete


        assertEquals("redirect:/provinces", view); // Redirige al listado
        assertEquals("Provincia no encontrada", ra.getFlashAttributes().get("errorMessage")); // Flash error
    }

    @Test
    @DisplayName("listProvinces ERROR -> añade errorMessage i18n y devuelve vista listado")
    void listProvinces_error() {
        Pageable pageable = defaultPageable(); // Pageable típico
        Model model = new ExtendedModelMap();  // Model real para leer atributos
        Locale locale = new Locale("es");      // Locale de prueba


        when(provinceService.list(pageable))   // Simulamos que listar provincias falla...
                .thenThrow(new RuntimeException("boom")); // ...lanzando una excepción


        when(messageSource.getMessage(eq("msg.province-controller.list.error"), any(), eq(locale)))
                // Simulamos que el MessageSource, cuando le piden esa key de error, devuelve este texto
                .thenReturn("Error listando provincias");


        String view = controller.listProvinces(pageable, model, locale); // Ejecutamos el método del controlador


        assertEquals("views/province/province-list", view); // Aunque falle, el controlador vuelve a la vista de listado
        assertEquals("Error listando provincias", model.getAttribute("errorMessage")); // Debe poner un error en el model
        verify(messageSource).getMessage(eq("msg.province-controller.list.error"), any(), eq(locale)); // Verificamos i18n
    }
// =========================
// GET /provinces/edit?id=...  (showEditForm)
// =========================


    @Test
    @DisplayName("showEditForm OK -> devuelve form, mete province + listRegions")
    void showEditForm_ok() {
        Long id = 1L; // ID de provincia de prueba
        Model model = new ExtendedModelMap(); // Model real para inspeccionar atributos
        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real por si el controlador lo usa
        Locale locale = new Locale("es"); // Locale de prueba


        ProvinceUpdateDTO dto = new ProvinceUpdateDTO();  // DTO real para simular “lo que trae el service”
        when(provinceService.getForEdit(id)).thenReturn(dto); // Stub: al pedir “para editar”, devolvemos el dto


        stubListRegionsForSelect(); // Stub: regiones para el select del formulario


        String view = controller.showEditForm(id, model, locale, ra); // Ejecutamos el método


        assertEquals("views/province/province-form", view); // Debe devolver la vista del formulario
        assertTrue(model.containsAttribute("province"));     // Debe incluir el DTO de edición en el model
        assertTrue(model.containsAttribute("listRegions"));  // Debe incluir las regiones en el model
        verify(provinceService).getForEdit(id);              // Verificamos que pidió el DTO al service
        verify(provinceService).listRegionsForSelect();      // Verificamos que pidió las regiones al service
    }


    @Test
    @DisplayName("showEditForm NOT FOUND -> redirect listado + flash errorMessage")
    void showEditForm_notFound() {
        Long id = 99L; // ID que “no existe”
        Model model = new ExtendedModelMap(); // Model real (aunque aquí no lo use tanto)
        RedirectAttributes ra = new RedirectAttributesModelMap(); // Aquí sí nos interesa porque guarda flash attributes
        Locale locale = new Locale("es"); // Locale español


        when(provinceService.getForEdit(id))
                // Simulamos que el service no encuentra la provincia y lanza ResourceNotFoundException
                .thenThrow(new ResourceNotFoundException("Province", "id", id));


        when(messageSource.getMessage(eq("msg.province-controller.edit.notfound"), any(), eq(locale)))
                // Simulamos el texto i18n que el controlador pondrá como flash errorMessage
                .thenReturn("Provincia no encontrada");


        String view = controller.showEditForm(id, model, locale, ra); // Ejecutamos el método


        assertEquals("redirect:/provinces", view); // Debe redirigir al listado
        assertEquals("Provincia no encontrada", ra.getFlashAttributes().get("errorMessage")); // Debe meter flash error
    }

    @Test
    @DisplayName("insertProvince DUPLICATE -> redirect /new + flash errorMessage")
    void insert_duplicate() {
        ProvinceCreateDTO dto = mock(ProvinceCreateDTO.class); // DTO mock


        BindingResult br = mock(BindingResult.class); // BindingResult mock
        when(br.hasErrors()).thenReturn(false); // Simulamos: no hay errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // Aquí interesa porque guardamos flash attributes
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        doThrow(new DuplicateResourceException("Province", "code", "SE"))
                // Simulamos que al crear, el service detecta duplicado y lanza la excepción
                .when(provinceService).create(dto);


        when(messageSource.getMessage(eq("msg.province-controller.insert.codeExist"), any(), eq(locale)))
                // Simulamos el mensaje i18n que se mostrará al usuario
                .thenReturn("Código ya existe");


        String view = controller.insertProvince(dto, br, ra, model, locale); // Ejecutamos el insert


        assertEquals("redirect:/provinces/new", view); // En duplicado, el controlador redirige al formulario de nuevo
        assertEquals("Código ya existe", ra.getFlashAttributes().get("errorMessage")); // Debe poner flash error
    }


// =========================
// POST /provinces/update  (updateProvince)
// =========================


    @Test
    @DisplayName("updateProvince OK -> update y redirect listado")
    void update_ok() {
        ProvinceUpdateDTO dto = mock(ProvinceUpdateDTO.class); // DTO mock


        BindingResult br = mock(BindingResult.class); // BindingResult mock
        when(br.hasErrors()).thenReturn(false); // Simulamos: sin errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        String view = controller.updateProvince(dto, br, ra, model, locale); // Ejecutamos update


        assertEquals("redirect:/provinces", view); // En OK redirige al listado
        verify(provinceService).update(dto); // Debe llamar al service para actualizar
    }


    @Test
    @DisplayName("updateProvince VALIDATION -> vuelve a form y recarga listRegions")
    void update_validationErrors() {
        ProvinceUpdateDTO dto = mock(ProvinceUpdateDTO.class); // DTO mock (no importa contenido)


        BindingResult br = mock(BindingResult.class); // BindingResult mock
        when(br.hasErrors()).thenReturn(true); // Simulamos: hay errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // RedirectAttributes real
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        stubListRegionsForSelect(); // En error, hay que recargar regiones para volver a pintar el formulario


        String view = controller.updateProvince(dto, br, ra, model, locale); // Ejecutamos update


        assertEquals("views/province/province-form", view); // Si hay errores, vuelve al form
        assertTrue(model.containsAttribute("listRegions"));  // Debe existir listRegions para la vista
        verify(provinceService).listRegionsForSelect();      // Debe pedir regiones al service
        verify(provinceService, never()).update(any());      // Con errores NO debe actualizar
    }


    @Test
    @DisplayName("updateProvince DUPLICATE -> redirect edit?id=... + flash")
    void update_duplicate() {
        ProvinceUpdateDTO dto = mock(ProvinceUpdateDTO.class); // DTO mock
        when(dto.getId()).thenReturn(1L); // El controlador necesita el id para construir la URL de redirect
        // Ojo: este stub SOLO debe existir si el controlador realmente llama a getCode(); si no, Mockito strict stubs podría quejarse.
        when(dto.getId()).thenReturn(1L);


        BindingResult br = mock(BindingResult.class); // BindingResult mock
        when(br.hasErrors()).thenReturn(false); // No hay errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // Aquí interesa para flash attributes
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        doThrow(new DuplicateResourceException("Province", "code", "SE"))
                // Simulamos que al actualizar, el service detecta duplicado
                .when(provinceService).update(dto);


        when(messageSource.getMessage(eq("msg.province-controller.update.codeExist"), any(), eq(locale)))
                // Simulamos mensaje i18n para duplicado en update
                .thenReturn("Código ya existe para otra provincia");


        String view = controller.updateProvince(dto, br, ra, model, locale); // Ejecutamos update


        assertEquals("redirect:/provinces/edit?id=1", view); // Debe redirigir a editar esa provincia
        assertEquals("Código ya existe para otra provincia", ra.getFlashAttributes().get("errorMessage")); // Flash error
    }


    @Test
    @DisplayName("updateProvince NOT FOUND -> redirect listado + flash")
    void update_notFound() {
        ProvinceUpdateDTO dto = mock(ProvinceUpdateDTO.class); // DTO mock


        BindingResult br = mock(BindingResult.class); // BindingResult mock
        when(br.hasErrors()).thenReturn(false); // Sin errores de validación


        RedirectAttributes ra = new RedirectAttributesModelMap(); // Para flash attributes
        Model model = new ExtendedModelMap(); // Model real
        Locale locale = new Locale("es"); // Locale


        doThrow(new ResourceNotFoundException("Province", "id", 99L))
                // Simulamos que no se encuentra al actualizar
                .when(provinceService).update(dto);


        when(messageSource.getMessage(eq("msg.province-controller.detail.notFound"), any(), eq(locale)))
                // Mensaje i18n para “no encontrada”
                .thenReturn("Provincia no encontrada");


        String view = controller.updateProvince(dto, br, ra, model, locale); // Ejecutamos update


        assertEquals("redirect:/provinces", view); // En notFound redirige al listado
        assertEquals("Provincia no encontrada", ra.getFlashAttributes().get("errorMessage")); // Flash error
    }
// =========================
// GET /provinces/detail?id=...  (showDetail)
// =========================


    @Test
    @DisplayName("showDetail OK -> vista detalle + model province")
    void detail_ok() {
        Model model = new ExtendedModelMap(); // Model real
        RedirectAttributes ra = new RedirectAttributesModelMap(); // Por si hay redirect (en este caso no debería)
        Locale locale = new Locale("es"); // Locale


        when(provinceService.getDetail(1L)) // Stub: al pedir detalle 1...
                .thenReturn(new ProvinceDetailDTO()); // ...devolvemos un DTO de detalle


        String view = controller.showDetail(1L, model, ra, locale); // Ejecutamos showDetail


        assertEquals("views/province/province-detail", view); // Debe devolver la vista detalle
        assertTrue(model.containsAttribute("province")); // Debe meter el DTO en el model con clave "province"
        verify(provinceService).getDetail(1L); // Verificamos llamada al service
    }


    @Test
    @DisplayName("showDetail NOT FOUND -> redirect listado + flash")
    void detail_notFound() {
        Model model = new ExtendedModelMap(); // Model real
        RedirectAttributes ra = new RedirectAttributesModelMap(); // Para flash attributes
        Locale locale = new Locale("es"); // Locale


        when(provinceService.getDetail(99L))
                // Simulamos que no existe el detalle
                .thenThrow(new ResourceNotFoundException("Province", "id", 99L));


        when(messageSource.getMessage(eq("msg.province-controller.detail.notFound"), any(), eq(locale)))
                // Mensaje i18n
                .thenReturn("Provincia no encontrada");


        String view = controller.showDetail(99L, model, ra, locale); // Ejecutamos showDetail


        assertEquals("redirect:/provinces", view); // En notFound redirige al listado
        assertEquals("Provincia no encontrada", ra.getFlashAttributes().get("errorMessage")); // Flash error
    }
}
