// Variables y constantes
let nombre = "César";
const PI = 3.1416;

// Template literals
let saludo = `Hola, ${nombre}!`;

// Arrow functions
const suma = (a, b) => a + b;

// Destructuración
const persona = { nombre: "Ana", edad: 28 };
const { nombre: nombrePersona, edad } = persona;

// Parámetros por defecto y rest/spread
function multiplicar(factor = 2, ...numeros) {
  return numeros.map(n => n * factor);
}

// Clases y herencia
class Animal {
  constructor(nombre) {
    this.nombre = nombre;
  }
  hablar() {
    return `${this.nombre} hace un ruido.`;
  }
}

class Perro extends Animal {
  hablar() {
    return `${this.nombre} dice: ¡Guau!`;
  }
}

const miPerro = new Perro("Rex");

// Promesas y async/await
function esperar(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function demoAsync() {
  await esperar(100);
  return "Listo!";
}

// Módulos (solo sintaxis, requiere entorno compatible)
// export default suma;
// import suma from './es6.js';

// Map, Set, Symbol
const mapa = new Map();
mapa.set("clave", 123);

const conjunto = new Set([1, 2, 3, 3]);

const simbolo = Symbol("unico");

// Generadores
function* generador() {
  yield 1;
  yield 2;
  yield 3;
}

// Uso de for...of
for (let valor of generador()) {
  // console.log(valor);
}

// Spread operator en arrays y objetos
const arr1 = [1, 2];
const arr2 = [...arr1, 3, 4];

const obj1 = { a: 1, b: 2 };
const obj2 = { ...obj1, c: 3 };

// Valores por consola para pruebas
console.log(saludo);
console.log(suma(2, 3));
console.log(nombrePersona, edad);
console.log(multiplicar(3, 1, 2, 3));
console.log(miPerro.hablar());
demoAsync().then(console.log);
console.log([...mapa.entries()]);
console.log([...conjunto]);
console.log(simbolo.toString());
console.log(arr2);
console.log(obj2);