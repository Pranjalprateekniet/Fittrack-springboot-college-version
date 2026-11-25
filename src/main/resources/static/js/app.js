document.addEventListener("DOMContentLoaded", function () {
  // Toggle workout fields between STRENGTH (sets/reps) and CARDIO/YOGA (durationMinutes)
  const categorySelect = document.getElementById("workout-category");
  const strengthFields = document.getElementById("strength-fields");
  const durationFields = document.getElementById("duration-fields");

  function updateWorkoutFields() {
    if (!categorySelect || !strengthFields || !durationFields) return;
    const isStrength = categorySelect.value === "STRENGTH";
    strengthFields.style.display = isStrength ? "grid" : "none";
    durationFields.style.display = isStrength ? "none" : "block";
  }

  if (categorySelect) {
    categorySelect.addEventListener("change", updateWorkoutFields);
    updateWorkoutFields();
  }

  // Toggle inline edit forms
  document.querySelectorAll("[data-toggle-edit]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const targetId = btn.getAttribute("data-toggle-edit");
      const target = document.getElementById(targetId);
      if (target) {
        target.style.display = target.style.display === "none" ? "table-row" : "none";
      }
    });
  });

  // Serving preset quick-fill buttons
  document.querySelectorAll("[data-serving-grams]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const grams = btn.getAttribute("data-serving-grams");
      const inputId = btn.getAttribute("data-target-input");
      const input = document.getElementById(inputId);
      if (input && grams) {
        input.value = grams;
      }
    });
  });
});

