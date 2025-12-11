package com.tienda.controller;

import com.tienda.domain.Producto;
import com.tienda.service.CategoriaService;
import com.tienda.service.ProductoService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService, MessageSource messageSource) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.messageSource = messageSource;
    }

    // LISTADO
    @GetMapping("/listado")
    public String listado(Model model) {
        var productos = productoService.getProductos(false);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/producto/listado";
    }

    // GUARDAR / ACTUALIZAR
    @PostMapping("/guardar")
    public String guardar(Producto producto, RedirectAttributes redirectAttributes) {
        productoService.save(producto);
        redirectAttributes.addFlashAttribute(
                "todoOk",
                messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault())
        );
        return "redirect:/producto/listado";
    }

    
     
    // ELIMINAR
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProducto, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";

        try {
            Producto producto = new Producto();
            producto.setIdProducto(idProducto);
            productoService.delete(producto);
        } catch (Exception e) {
            titulo = "error";
            detalle = "producto.error03";
        }

        redirectAttributes.addFlashAttribute(
                titulo,
                messageSource.getMessage(detalle, null, Locale.getDefault())
        );
        return "redirect:/producto/listado";
    }

    // MODIFICAR
    @GetMapping("/modificar/{idProducto}")
    public String modificar(@PathVariable Integer idProducto, Model model, RedirectAttributes redirectAttributes) {
        Producto producto = new Producto();
        producto.setIdProducto(idProducto);

        producto = productoService.getProducto(producto);

        if (producto == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    messageSource.getMessage("producto.error01", null, Locale.getDefault())
            );
            return "redirect:/producto/listado";
        }

        model.addAttribute("producto", producto);
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);

        return "/producto/modifica";
    }
}
