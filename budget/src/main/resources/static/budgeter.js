// budget.js

// API endpoint
const apiUrl = '/user/budget';

// Selectors
const monthSelect = document.getElementById('month-select');
const yearSelect = document.getElementById('year-select');
const incomeAmount = document.getElementById('income-amount');
const totalExpensesAmount = document.getElementById('total-expenses');
const expensesContainer = document.getElementById('expenses-container');
const expenseChartCtx = document.getElementById('expense-chart').getContext('2d');

let expenseChart = null; // store chart instance

// Listen for changes
monthSelect.addEventListener('change', fetchAndDisplayBudget);
yearSelect.addEventListener('change', fetchAndDisplayBudget);

// Helper to capitalize category names
function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// Fetch budget data from API
async function fetchBudget() {
    try {
        const response = await fetch(apiUrl);
        if (!response.ok) throw new Error('Failed to fetch budget');
        const data = await response.json();
        return data;
    } catch (err) {
        console.error(err);
        return null;
    }
}

// Filter and categorize expenses based on selected month/year
function filterBudget(budget, month, year) {
    // Assume each expense has { category, amount, month, year }
    const filteredExpenses = budget.expenses.filter(
        exp => exp.month === month && exp.year === year
    );

    // Group expenses by category
    const categorized = {};
    filteredExpenses.forEach(exp => {
        if (!categorized[exp.category]) categorized[exp.category] = 0;
        categorized[exp.category] += exp.amount;
    });

    return {
        income: budget.income || 0,
        expenses: categorized
    };
}

// Update the DOM with filtered budget
function displayBudget(budget) {
    incomeAmount.textContent = `$${budget.income.toFixed(2)}`;

    let total = 0;
    expensesContainer.innerHTML = ''; // clear previous
    for (const [category, amount] of Object.entries(budget.expenses)) {
        total += amount;

        const div = document.createElement('div');
        div.className = 'expense-item';
        div.textContent = `${capitalize(category)}: $${amount.toFixed(2)}`;
        expensesContainer.appendChild(div);
    }

    totalExpensesAmount.textContent = `$${total.toFixed(2)}`;

    updateChart(budget.expenses);
}

// Update the Chart.js chart
function updateChart(expenses) {
    const categories = Object.keys(expenses);
    const amounts = Object.values(expenses);

    if (expenseChart) {
        // Update existing chart
        expenseChart.data.labels = categories;
        expenseChart.data.datasets[0].data = amounts;
        expenseChart.update();
    } else {
        // Create chart for the first time
        expenseChart = new Chart(expenseChartCtx, {
            type: 'pie',
            data: {
                labels: categories,
                datasets: [{
                    label: 'Expenses',
                    data: amounts,
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0',
                        '#9966FF',
                        '#FF9F40'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `${context.label}: $${context.raw.toFixed(2)}`;
                            }
                        }
                    }
                }
            }
        });
    }
}

// Main function
async function fetchAndDisplayBudget() {
    const month = monthSelect.value;
    const year = yearSelect.value;

    //window.alert(month, year);

    const budget = await fetchBudget();
    if (!budget) return;

    const filtered = filterBudget(budget, month, year);
    displayBudget(filtered);
}

// Initial load
fetchAndDisplayBudget();
