const example = document.querySelector('#example')
const message = document.querySelector('#message')

example.addEventListener('click', () => {
    message.value = document.querySelector('#sample').content.textContent.trim()
    message.focus()
})

document.querySelector('form').addEventListener('submit', (event) => {
    const submit = event.currentTarget.querySelector('button[type="submit"]')
    submit.disabled = true
    submit.textContent = 'Обробляємо цитування…'
})
window.addEventListener('pageshow', () => {
    const submit = document.querySelector('button[type="submit"]')
    submit.disabled = false
    submit.textContent = 'Структурувати цитування'
})
