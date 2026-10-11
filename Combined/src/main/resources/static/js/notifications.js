document.addEventListener("DOMContentLoaded", function () {
    var filterButtons = document.querySelectorAll("[data-filter]");
    var notificationItems = document.querySelectorAll(".notification-item");

    if (!filterButtons.length || !notificationItems.length) {
        return;
    }

    function applyFilter(filterValue) {
        notificationItems.forEach(function (item) {
            var readStatus = String(item.getAttribute("data-read") || "false");
            var shouldShow = filterValue === "all"
                || (filterValue === "unread" && readStatus === "false")
                || (filterValue === "read" && readStatus === "true");

            item.hidden = !shouldShow;
        });
    }

    filterButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            var selectedFilter = button.getAttribute("data-filter") || "all";

            filterButtons.forEach(function (currentButton) {
                var isActive = currentButton === button;
                currentButton.classList.toggle("active", isActive);
                currentButton.setAttribute("aria-pressed", String(isActive));
            });

            applyFilter(selectedFilter);
        });
    });

    var activeButton = document.querySelector("[data-filter].active");
    applyFilter((activeButton ? activeButton.getAttribute("data-filter") : "all"));
});
