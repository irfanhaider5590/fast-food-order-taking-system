package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptPrintService {

    private final SettingsService settingsService;

    @Value("${app.receipt.auto-print-enabled:false}")
    private boolean autoPrintEnabled;

    @Value("${app.receipt.printer-name:}")
    private String printerName;

    @Value("${app.brand.name:Fast Food Express}")
    private String defaultBrandName;

    @Value("${app.brand.location:Gujranwala, Pakistan}")
    private String defaultBrandLocation;

    public void printReceipt(OrderResponse order) {
        if (!autoPrintEnabled) {
            log.debug("Auto-print is disabled, skipping receipt printing");
            return;
        }

        try {
            String receiptContent = generateReceiptContent(order);
            printToPrinter(receiptContent);
            log.info("Receipt printed successfully for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Error printing receipt for order: {}", order.getOrderNumber(), e);
        }
    }

    private String generateReceiptContent(OrderResponse order) {
        StringBuilder receipt = new StringBuilder();
        
        // Get brand info from settings
        String brandName = getBrandName();
        String brandLocation = getBrandLocation();
        
        // Header with logo placeholder
        receipt.append("================================\n");
        receipt.append("     ").append(brandName).append("\n");
        receipt.append("   ").append(brandLocation).append("\n");
        receipt.append("================================\n\n");
        
        // Order details
        receipt.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        receipt.append("Date: ").append(formatDateTime(order.getOrderDate())).append("\n");
        receipt.append("Type: ").append(order.getOrderType()).append("\n");
        
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            receipt.append("Customer: ").append(order.getCustomerName()).append("\n");
        }
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            receipt.append("Phone: ").append(order.getCustomerPhone()).append("\n");
        }
        if (order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            receipt.append("Table: ").append(order.getTableNumber()).append("\n");
        }
        if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
            receipt.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        }
        
        receipt.append("--------------------------------\n");
        
        // Order items
        receipt.append("Items:\n");
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                receipt.append(String.format("%-20s %2dx %8.2f\n", 
                    truncate(item.getItemNameEn(), 20), 
                    item.getQuantity(), 
                    item.getTotalPrice().doubleValue()));
            }
        }
        
        receipt.append("--------------------------------\n");
        
        // Totals
        receipt.append(String.format("Subtotal:        %10.2f\n", order.getSubtotal().doubleValue()));
        if (order.getDiscountAmount() != null && order.getDiscountAmount().doubleValue() > 0) {
            receipt.append(String.format("Discount:        %10.2f\n", order.getDiscountAmount().doubleValue()));
            if (order.getVoucherCode() != null) {
                receipt.append("Voucher: ").append(order.getVoucherCode()).append("\n");
            }
        }
        receipt.append(String.format("Total:            %10.2f\n", order.getTotalAmount().doubleValue()));
        
        receipt.append("--------------------------------\n");
        receipt.append("Payment: ").append(order.getPaymentMethod()).append("\n");
        receipt.append("Status: ").append(order.getOrderStatus()).append("\n");
        
        receipt.append("\n");
        receipt.append("Thank you for your order!\n");
        receipt.append("================================\n");
        
        return receipt.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void printToPrinter(String content) throws PrintException, IOException {
        PrintService printService = findPrintService();
        if (printService == null) {
            log.warn("No printer found. Please check printer connection.");
            return;
        }

        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Doc doc = new SimpleDoc(inputStream, flavor, null);

        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(new Copies(1));
        attributes.add(OrientationRequested.PORTRAIT);
        attributes.add(MediaSizeName.ISO_A4);

        DocPrintJob printJob = printService.createPrintJob();
        printJob.print(doc, attributes);
        
        log.info("Print job submitted to printer: {}", printService.getName());
    }

    private PrintService findPrintService() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        
        if (services.length == 0) {
            log.warn("No printers found on the system");
            return null;
        }

        // If specific printer name is configured, try to find it
        if (printerName != null && !printerName.isEmpty()) {
            for (PrintService service : services) {
                if (service.getName().equalsIgnoreCase(printerName)) {
                    log.info("Found configured printer: {}", printerName);
                    return service;
                }
            }
            log.warn("Configured printer '{}' not found, using default printer", printerName);
        }

        // Use default printer
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService != null) {
            log.info("Using default printer: {}", defaultService.getName());
            return defaultService;
        }

        // Fallback to first available printer
        log.info("Using first available printer: {}", services[0].getName());
        return services[0];
    }

    public String generateReceiptHtml(OrderResponse order) {
        // Get brand info from settings
        String brandName = getBrandName();
        String brandLocation = getBrandLocation();
        String brandLogoUrl = getBrandLogoUrl();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<style>\n");
        html.append("@page { size: 80mm auto; margin: 5mm; }\n");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 10px; width: 70mm; font-size: 12px; }\n");
        html.append(".header { text-align: center; margin-bottom: 15px; }\n");
        html.append(".logo-img { max-width: 60mm; max-height: 30mm; margin-bottom: 8px; display: block; margin-left: auto; margin-right: auto; }\n");
        html.append(".logo { font-size: 18px; font-weight: bold; margin-bottom: 3px; }\n");
        html.append(".location { font-size: 11px; color: #666; }\n");
        html.append(".section { margin: 10px 0; font-size: 11px; }\n");
        html.append(".items { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 11px; }\n");
        html.append(".items th, .items td { padding: 4px 2px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append(".items th { background-color: #f5f5f5; font-size: 10px; }\n");
        html.append(".items td { font-size: 10px; }\n");
        html.append(".total { font-weight: bold; font-size: 12px; margin-top: 8px; }\n");
        html.append(".footer { text-align: center; margin-top: 15px; color: #666; font-size: 10px; }\n");
        html.append("@media print { body { margin: 0; padding: 5mm; } .no-print { display: none; } }\n");
        html.append("@media screen { body { margin: 10px auto; } }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        // Header
        html.append("<div class='header'>\n");
        if (brandLogoUrl != null && !brandLogoUrl.isEmpty()) {
            html.append("<img src='").append(escapeHtml(brandLogoUrl)).append("' alt='Logo' class='logo-img' />\n");
        }
        html.append("<div class='logo'>").append(escapeHtml(brandName)).append("</div>\n");
        html.append("<div class='location'>").append(escapeHtml(brandLocation)).append("</div>\n");
        html.append("</div>\n");
        
        // Order details
        html.append("<div class='section'>\n");
        html.append("<strong>Order Number:</strong> ").append(order.getOrderNumber()).append("<br>\n");
        html.append("<strong>Date:</strong> ").append(formatDateTime(order.getOrderDate())).append("<br>\n");
        html.append("<strong>Type:</strong> ").append(order.getOrderType()).append("<br>\n");
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            html.append("<strong>Customer:</strong> ").append(escapeHtml(order.getCustomerName())).append("<br>\n");
        }
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            html.append("<strong>Phone:</strong> ").append(escapeHtml(order.getCustomerPhone())).append("<br>\n");
        }
        if (order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            html.append("<strong>Table:</strong> ").append(escapeHtml(order.getTableNumber())).append("<br>\n");
        }
        if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
            html.append("<strong>Address:</strong> ").append(escapeHtml(order.getDeliveryAddress())).append("<br>\n");
        }
        html.append("</div>\n");
        
        // Items table
        html.append("<table class='items'>\n");
        html.append("<tr><th>Item</th><th>Qty</th><th>Price</th></tr>\n");
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(item.getItemNameEn())).append("</td>");
                html.append("<td>").append(item.getQuantity()).append("</td>");
                html.append("<td>").append(String.format("%.2f", item.getTotalPrice().doubleValue())).append("</td>");
                html.append("</tr>\n");
            }
        }
        html.append("</table>\n");
        
        // Totals
        html.append("<div class='section'>\n");
        html.append("<strong>Subtotal:</strong> ").append(String.format("%.2f", order.getSubtotal().doubleValue())).append("<br>\n");
        if (order.getDiscountAmount() != null && order.getDiscountAmount().doubleValue() > 0) {
            html.append("<strong>Discount:</strong> ").append(String.format("%.2f", order.getDiscountAmount().doubleValue())).append("<br>\n");
            if (order.getVoucherCode() != null) {
                html.append("<strong>Voucher:</strong> ").append(escapeHtml(order.getVoucherCode())).append("<br>\n");
            }
        }
        html.append("<div class='total'>Total: ").append(String.format("%.2f", order.getTotalAmount().doubleValue())).append("</div>\n");
        html.append("<strong>Payment:</strong> ").append(order.getPaymentMethod()).append("<br>\n");
        html.append("<strong>Status:</strong> ").append(order.getOrderStatus()).append("<br>\n");
        html.append("</div>\n");
        
        // Footer
        html.append("<div class='footer'>\n");
        html.append("Thank you for your order!\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String getBrandName() {
        try {
            var settings = settingsService.getSettings();
            return settings.getBrandName() != null && !settings.getBrandName().isEmpty() 
                    ? settings.getBrandName() 
                    : defaultBrandName;
        } catch (Exception e) {
            log.warn("Error fetching brand name from settings, using default", e);
            return defaultBrandName;
        }
    }

    private String getBrandLocation() {
        try {
            var settings = settingsService.getSettings();
            return settings.getAddress() != null && !settings.getAddress().isEmpty() 
                    ? settings.getAddress() 
                    : defaultBrandLocation;
        } catch (Exception e) {
            log.warn("Error fetching brand location from settings, using default", e);
            return defaultBrandLocation;
        }
    }

    private String getBrandLogoUrl() {
        try {
            var settings = settingsService.getSettings();
            return settings.getBrandLogoUrl();
        } catch (Exception e) {
            log.warn("Error fetching brand logo from settings", e);
            return null;
        }
    }
}

