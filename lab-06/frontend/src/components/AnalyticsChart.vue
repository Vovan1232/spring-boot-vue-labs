<script setup>
import Chart from 'chart.js/auto'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  type: { type: String, required: true },
  rows: { type: Array, required: true },
  label: { type: String, required: true },
})

const canvas = ref(null)
const colors = {
  ukr: '#2d654b',
  eng: '#b15f3e',
  other: '#c29b53',
  unknown: '#78878d',
}
const percent = new Intl.NumberFormat('uk', { style: 'percent', maximumFractionDigits: 1 })
const sum = computed(() => props.rows.reduce((total, row) => total + row.count, 0))
let chart

function draw() {
  chart?.destroy()
  const pie = props.type === 'pie'
  const rows = pie ? props.rows : props.rows.slice(0, 12)
  const total = rows.reduce((sum, row) => sum + row.count, 0)
  chart = new Chart(canvas.value, {
    type: props.type,
    data: {
      labels: rows.map((row) => row.label),
      datasets: [{
        label: 'Статті',
        data: rows.map((row) => row.count),
        backgroundColor: pie ? rows.map((row) => colors[row.code]) : '#2d654b',
        borderWidth: pie ? 2 : 0,
        borderColor: '#fffdf7',
        borderRadius: pie ? 0 : 4,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      color: '#465247',
      indexAxis: pie ? 'x' : 'y',
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ({ raw }) => pie
              ? raw + ' · ' + percent.format(raw / total)
              : raw + ' статей',
          },
        },
      },
      scales: pie ? {} : {
        x: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: '#e8e8dc' } },
        y: {
          grid: { display: false },
          ticks: {
            callback(value) {
              const label = this.getLabelForValue(value)
              return label.length > 25 ? label.slice(0, 24) + '…' : label
            },
          },
        },
      },
    },
  })
}

onMounted(draw)
watch(() => props.rows, draw, { flush: 'post' })
onBeforeUnmount(() => chart?.destroy())
</script>

<template>
  <div class="analytics-canvas" :class="{ 'analytics-canvas-pie': type === 'pie' }">
    <canvas ref="canvas" role="img" :aria-label="label"></canvas>
  </div>
  <ul v-if="type === 'pie'" class="chart-legend" :aria-label="label">
    <li v-for="row in rows" :key="row.code">
      <span class="legend-dot" :style="{ backgroundColor: colors[row.code] }"></span>
      <span>{{ row.label }}</span>
      <strong>{{ percent.format(row.count / sum) }}</strong>
      <span class="text-secondary">({{ row.count }})</span>
    </li>
  </ul>
  <details v-else class="chart-data">
    <summary>{{ rows.length > 12 ? 'Показано 12 найбільших категорій. Усі дані' : 'Дані графіка' }}</summary>
    <dl class="chart-counts">
      <div v-for="row in rows" :key="row.label">
        <dt>{{ row.label }}</dt><dd>{{ row.count }}</dd>
      </div>
    </dl>
  </details>
</template>
