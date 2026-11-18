import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Products from './pages/Products';
import MyProducts from './pages/MyProducts';
import ProductForm from './pages/ProductForm';
import PlaceOrder from './pages/PlaceOrder';
import BuyerOrders from './pages/BuyerOrders';
import FarmerOrders from './pages/FarmerOrders';
import FarmerProfile from './pages/FarmerProfile';
import BuyerProfile from './pages/BuyerProfile';
import AiChat from './pages/AiChat';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          <Route path="/products" element={
            <ProtectedRoute allowedRoles={['FARMER', 'BUYER', 'ADMIN']}>
              <Products />
            </ProtectedRoute>
          } />

          <Route path="/products/my" element={
            <ProtectedRoute allowedRoles={['FARMER']}>
              <MyProducts />
            </ProtectedRoute>
          } />

          <Route path="/products/new" element={
            <ProtectedRoute allowedRoles={['FARMER']}>
              <ProductForm />
            </ProtectedRoute>
          } />

          <Route path="/products/:id/edit" element={
            <ProtectedRoute allowedRoles={['FARMER']}>
              <ProductForm />
            </ProtectedRoute>
          } />

          <Route path="/orders/new" element={
            <ProtectedRoute allowedRoles={['BUYER']}>
              <PlaceOrder />
            </ProtectedRoute>
          } />

          <Route path="/orders/buyer" element={
            <ProtectedRoute allowedRoles={['BUYER']}>
              <BuyerOrders />
            </ProtectedRoute>
          } />

          <Route path="/orders/farmer" element={
            <ProtectedRoute allowedRoles={['FARMER']}>
              <FarmerOrders />
            </ProtectedRoute>
          } />

          <Route path="/farmer/profile" element={
            <ProtectedRoute allowedRoles={['FARMER']}>
              <FarmerProfile />
            </ProtectedRoute>
          } />

          <Route path="/buyer/profile" element={
            <ProtectedRoute allowedRoles={['BUYER']}>
              <BuyerProfile />
            </ProtectedRoute>
          } />

          <Route path="/ai" element={
            <ProtectedRoute allowedRoles={['FARMER', 'BUYER', 'ADMIN']}>
              <AiChat />
            </ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
