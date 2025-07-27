using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Memora.Migrations
{
    /// <inheritdoc />
    public partial class RemoveNombreCompletoUniqueConstraint : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_Usuarios_NombreCompleto",
                table: "Usuarios");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateIndex(
                name: "IX_Usuarios_NombreCompleto",
                table: "Usuarios",
                column: "NombreCompleto",
                unique: true);
        }
    }
}
