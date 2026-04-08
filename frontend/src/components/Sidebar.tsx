import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, Car, ReceiptText, LogOut, Settings } from 'lucide-react';

export default function Sidebar() {
  const navigate = useNavigate();
  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const roles = user?.roles || [];
  const isAdmin = roles.includes('ROLE_ADMIN');
  const isDealer = roles.includes('ROLE_DEALER');
  const isEmployee = roles.includes('ROLE_EMPLOYEE');

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard }
  ];

  if (isAdmin) {
    navItems.push({ name: 'Manage Dealers', path: '/dealers', icon: Users });
    navItems.push({ name: 'Global Sales', path: '/sales', icon: ReceiptText });
    navItems.push({ name: 'Audit Logs', path: '/audit', icon: ReceiptText });
  }

  if (isDealer) {
    navItems.push({ name: 'Manage Employees', path: '/employees', icon: Users });
    navItems.push({ name: 'Dealership Inventory', path: '/inventory', icon: Car });
    navItems.push({ name: 'Customers CRM', path: '/customers', icon: Users });
    navItems.push({ name: 'Dealership Sales', path: '/sales', icon: ReceiptText });
  }

  if (isEmployee) {
    navItems.push({ name: 'My Customers', path: '/customers', icon: Users });
    navItems.push({ name: 'My Sales Orders', path: '/sales', icon: ReceiptText });
    navItems.push({ name: 'View Inventory', path: '/inventory', icon: Car });
    navItems.push({ name: 'Test Drives', path: '/test-drives', icon: Car });
  }

  return (
    <aside className="w-64 bg-gray-900 text-white h-screen flex flex-col shadow-xl sticky top-0 flex-shrink-0">
      <div className="p-6 border-b border-gray-800">
        <h1 className="text-2xl font-bold text-white tracking-wider">HYUNDAI <span className="text-blue-500">DMS</span></h1>
        <p className="text-xs text-gray-400 mt-1">Logged in as: {user?.username}</p>
        {user?.dealerName && (
          <p className="text-xs text-blue-400 mt-0.5 font-medium">{user.dealerName}</p>
        )}
        {!user?.dealerName && isAdmin && (
          <p className="text-xs text-green-400 mt-0.5 font-medium">Global Admin</p>
        )}
      </div>

      <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
        {navItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center px-4 py-3 rounded-lg transition-colors ${
                isActive 
                  ? 'bg-blue-600 text-white shadow-md' 
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <item.icon className="h-5 w-5 mr-3" />
            <span className="font-medium">{item.name}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-800 mt-auto">
        <button
          onClick={handleLogout}
          className="flex items-center w-full px-4 py-3 text-sm font-medium text-gray-300 rounded-lg hover:bg-red-600 hover:text-white transition-colors"
        >
          <LogOut className="h-5 w-5 mr-3" />
          Sign Out
        </button>
      </div>
    </aside>
  );
}
