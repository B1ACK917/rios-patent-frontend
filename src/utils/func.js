export function GenNonDuplicateID(randomLength) {
    return Number(Math.random().toString().substr(2, randomLength) + Date.now()).toString(36)
}