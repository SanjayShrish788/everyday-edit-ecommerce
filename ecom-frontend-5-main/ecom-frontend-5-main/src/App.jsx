import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import './storefront.css';
import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import Navbar from './components/Navbar';
import Cart from './components/Cart';
import AddProduct from './components/AddProduct';
import Product from './components/Product';
import UpdateProduct from './components/UpdateProduct';
export default function App() {
  const [searchQuery,setSearchQuery] = useState('');
  return <BrowserRouter><Navbar onSearch={setSearchQuery} /><Routes><Route path="/" element={<Home searchQuery={searchQuery} />} /><Route path="/add_product" element={<AddProduct />} /><Route path="/product" element={<Product />} /><Route path="/product/:id" element={<Product />} /><Route path="/cart" element={<Cart />} /><Route path="/product/update/:id" element={<UpdateProduct />} /></Routes></BrowserRouter>;
}


