/* ==================================================
   DEFENSE LOGISTICS — SHARED UI UTILITIES
   ================================================== */

(function () {
    "use strict";

    /**
     * Find the first focusable element within a container.
     * Returns null when no suitable element exists.
     */
    function getFirstFocusable(container) {
        if (!container) {
            return null;
        }

        return container.querySelector(
            'a[href], button:not([disabled]), ' +
            'input:not([disabled]), select:not([disabled]), ' +
            'textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
        );
    }

    /**
     * Focus a container's first focusable element.
     * This utility does not perform authentication or authorization.
     */
    function focusFirst(container) {
        var element = getFirstFocusable(container);

        if (element) {
            element.focus();
        }
    }

    /**
     * Mark the current page's navigation links as active.
     *
     * A link must explicitly use data-nav-path="/path".
     * Links without data-nav-path are left unchanged.
     *
     * This is visual navigation behavior only.
     * Server-side security must protect application routes.
     */
    function updateActiveNavigation() {
        var currentPath = window.location.pathname;
        var links = document.querySelectorAll("[data-nav-path]");

        links.forEach(function (link) {
            var targetPath = link.getAttribute("data-nav-path");

            if (!targetPath) {
                return;
            }

            var isActive = currentPath === targetPath;

            if (isActive) {
                link.classList.add("active");
                link.setAttribute("aria-current", "page");
            } else {
                link.classList.remove("active");
                link.removeAttribute("aria-current");
            }
        });
    }

    /**
     * Set a visible loading state on an element.
     * This does not call APIs or create mock data.
     */
    function setLoading(element, isLoading, message) {
        if (!element) {
            return;
        }

        if (isLoading) {
            element.setAttribute("aria-busy", "true");

            if (message) {
                element.setAttribute("data-loading-message", message);
            }
        } else {
            element.removeAttribute("aria-busy");
            element.removeAttribute("data-loading-message");
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        updateActiveNavigation();
    });

    /*
     * Expose small reusable utilities for future page scripts.
     * No credentials, tokens or permissions are stored here.
     */
    window.DefenseLogisticsUI = Object.freeze({
        focusFirst: focusFirst,
        updateActiveNavigation: updateActiveNavigation,
        setLoading: setLoading
    });
})();
