import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, Car, ReceiptText, LogOut, ShieldAlert, Building2, ClipboardList, Menu, X } from 'lucide-react';

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;
  const [isOpen, setIsOpen] = useState(false);

  // Close sidebar on route change (mobile)
  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  // Close sidebar on window resize to desktop
  useEffect(() => {
    const handler = () => { if (window.innerWidth >= 1024) setIsOpen(false); };
    window.addEventListener('resize', handler);
    return () => window.removeEventListener('resize', handler);
  }, []);

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
    navItems.push({ name: 'Manage Dealers', path: '/dealers', icon: Building2 });
    navItems.push({ name: 'Global Sales', path: '/sales', icon: ReceiptText });
    navItems.push({ name: 'Audit Logs', path: '/audit', icon: ShieldAlert });
  }

  if (isDealer) {
    navItems.push({ name: 'Manage Employees', path: '/employees', icon: Users });
    navItems.push({ name: 'Dealership Inventory', path: '/inventory', icon: Car });
    navItems.push({ name: 'Customers CRM', path: '/customers', icon: Users });
    navItems.push({ name: 'Dealership Sales', path: '/sales', icon: ReceiptText });
  }

  if (isEmployee) {
    navItems.push({ name: 'My Customers', path: '/customers', icon: Users });
    navItems.push({ name: 'My Sales', path: '/sales', icon: ReceiptText });
    navItems.push({ name: 'Inventory', path: '/inventory', icon: Car });
    navItems.push({ name: 'Test Drives', path: '/test-drives', icon: ClipboardList });
  }

  const sidebarContent = (
    <>
      <div className="p-6 border-b border-gray-800">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-white tracking-wider">HYUNDAI <span className="text-blue-500">DMS</span></h1>
            <p className="text-xs text-gray-400 mt-1">Logged in as: {user?.username}</p>
            {user?.dealerName && (
              <p className="text-xs text-blue-400 mt-0.5 font-medium">{user.dealerName}</p>
            )}
            {!user?.dealerName && isAdmin && (
              <p className="text-xs text-green-400 mt-0.5 font-medium">Global Admin</p>
            )}
          </div>
          {/* Close button on mobile */}
          <button
            onClick={() => setIsOpen(false)}
            className="lg:hidden p-1.5 text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
      </div>

      <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
        {navItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `flex items-center px-4 py-3 rounded-lg transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <item.icon className="h-5 w-5 mr-3 flex-shrink-0" />
            <span className="font-medium">{item.name}</span>
          </NavLink>
        ))}
      </nav>



      <div className="p-4 border-t border-gray-800">
        <button
          onClick={handleLogout}
          className="flex items-center w-full px-4 py-3 text-sm font-medium text-gray-300 rounded-lg hover:bg-red-600 hover:text-white transition-colors"
        >
          <LogOut className="h-5 w-5 mr-3" />
          Sign Out
        </button>
      </div>
    </>
  );

  return (
    <>
      {/* Hamburger button — visible only on mobile */}
      <button
        onClick={() => setIsOpen(true)}
        className="lg:hidden fixed top-3 left-3 z-[100] p-2 bg-gray-900 text-white rounded-lg shadow-lg hover:bg-gray-800 transition"
        aria-label="Open menu"
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Desktop sidebar — always visible on lg+ */}
      <aside className="hidden lg:flex w-64 bg-gray-900 text-white h-screen flex-col flex-shrink-0">
        {sidebarContent}
      </aside>

      {/* Mobile sidebar — slide-over with backdrop */}
      {isOpen && (
        <div className="lg:hidden fixed inset-0 z-[90]">
          <div
            className="absolute inset-0 bg-black/60"
            onClick={() => setIsOpen(false)}
          />
          <aside className="relative z-10 w-72 bg-gray-900 text-white h-full flex flex-col shadow-2xl">
            {sidebarContent}
          </aside>
        </div>
      )}
    </>
  );
}
